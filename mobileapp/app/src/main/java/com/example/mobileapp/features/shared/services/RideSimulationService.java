package com.example.mobileapp.features.shared.services;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RoutingApi;
import com.example.mobileapp.features.shared.api.dto.OsrmRouteResponse;
import com.example.mobileapp.features.shared.api.VehiclesApi;
import com.example.mobileapp.features.shared.api.dto.VehiclePositionUpdateDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideSimulationService {

    public interface Listener {
        void onTick(double lat, double lon);
        void onPickupArrived();
        void onArrived();
    }
    private boolean pickupNotified = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final VehiclesApi vehiclesApi = ApiClient.get().create(VehiclesApi.class);

    private final RoutingApi routingApi = ApiClient.getOsrm().create(RoutingApi.class);

    private boolean running = false;

    private int vehicleId;
    private String bearerToken;
    private Listener listener;

    // path po kojem se auto krece
    private final List<double[]> path = new ArrayList<>(); // [lat, lon]
    private final List<Integer> stopIndices = new ArrayList<>(); // indeksi u path gdje treba 10s pauza

    private int idx = 0;

    private long lastBackendSendMs = 0L;

    private final long pauseAtStopMs = 3_000L;
    private final long backendTickMs = 1000L;
    private final long uiTickMs = 200L;         // smooth
    private final long stepEveryMs = 1000L;      // pomjeri se na sljedecu path tacku svakih X ms

    private long lastStepMs = 0L;
    private long pausedUntilMs = 0L;

    // ~100m resample
    private final double stepMeters = 50.0;

    // stopPoints: lista stop tačaka: pickup, stop1, stop2, ..., destination
    public void startByRoute(
            int vehicleId,
            @NonNull String bearerToken,
            @NonNull List<double[]> stopPoints,
            @NonNull Listener listener
    ) {
        stop();

        if (stopPoints.size() < 2) return;

        this.vehicleId = vehicleId;
        this.bearerToken = bearerToken;
        this.listener = listener;

        buildRoutePath(stopPoints);
    }

    public void stop() {
        pickupNotified = false;
        running = false;
        handler.removeCallbacksAndMessages(null);
        path.clear();
        stopIndices.clear();
        idx = 0;
        pausedUntilMs = 0L;
    }

    public boolean isRunning() {
        return running;
    }

    private void buildRoutePath(@NonNull List<double[]> stopPoints) {
        path.clear();
        stopIndices.clear();

        fetchLegRecursive(stopPoints, 0, new ArrayList<>());
    }

    private void fetchLegRecursive(
            @NonNull List<double[]> stops,
            int legIndex,
            @NonNull List<double[]> accumulated // raw path points (not resampled)
    ) {
        if (legIndex >= stops.size() - 1) {
            List<double[]> resampled = resampleEveryMeters(accumulated, stepMeters);
            if (resampled.size() < 2) return;

            path.clear();
            path.addAll(resampled);

            for (int i = 1; i < stops.size(); i++) {
                int nearest = nearestIndex(path, stops.get(i));
                stopIndices.add(nearest);
            }

            double[] dest = stops.get(stops.size() - 1);
            path.set(path.size() - 1, new double[]{dest[0], dest[1]});

            running = true;
            idx = 0;
            lastBackendSendMs = 0L;
            lastStepMs = System.currentTimeMillis();
            pausedUntilMs = 0L;

            handler.post(tickRunnable);
            return;
        }

        double[] a = stops.get(legIndex);
        double[] b = stops.get(legIndex + 1);

        String coords = String.format(Locale.US,
                "%.6f,%.6f;%.6f,%.6f",
                a[1], a[0],
                b[1], b[0]
        );

        routingApi.route(coords, "full", "geojson").enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<OsrmRouteResponse> call,
                                   @NonNull Response<OsrmRouteResponse> response) {
                if (!response.isSuccessful() || response.body() == null ||
                        response.body().routes == null || response.body().routes.isEmpty() ||
                        response.body().routes.get(0).geometry == null ||
                        response.body().routes.get(0).geometry.coordinates == null) {
                    appendLine(accumulated, a, b);
                    fetchLegRecursive(stops, legIndex + 1, accumulated);
                    return;
                }

                List<List<Double>> coords = response.body().routes.get(0).geometry.coordinates;
                for (int i = 0; i < coords.size(); i++) {
                    List<Double> c = coords.get(i);
                    if (c == null || c.size() < 2) continue;
                    double lon = c.get(0);
                    double lat = c.get(1);

                    if (!accumulated.isEmpty()) {
                        double[] last = accumulated.get(accumulated.size() - 1);
                        if (distanceMeters(last[0], last[1], lat, lon) < 1.0) continue;
                    }
                    accumulated.add(new double[]{lat, lon});
                }

                fetchLegRecursive(stops, legIndex + 1, accumulated);
            }

            @Override
            public void onFailure(@NonNull Call<OsrmRouteResponse> call, @NonNull Throwable t) {
                appendLine(accumulated, a, b);
                fetchLegRecursive(stops, legIndex + 1, accumulated);
            }
        });
    }

    private void appendLine(List<double[]> acc, double[] a, double[] b) {
        if (acc.isEmpty()) acc.add(new double[]{a[0], a[1]});
        acc.add(new double[]{b[0], b[1]});
    }


    private final Runnable tickRunnable = new Runnable() {
        @Override public void run() {
            if (!running) return;

            long now = System.currentTimeMillis();

            if (idx >= path.size()) {
                running = false;
                if (listener != null) listener.onArrived();
                return;
            }

            if (now < pausedUntilMs) {
                emitCurrentPoint(now);
                handler.postDelayed(this, uiTickMs);
                return;
            }

            double[] p = path.get(idx);
            double lat = p[0];
            double lon = p[1];

            if (listener != null) listener.onTick(lat, lon);

            if (now - lastBackendSendMs >= backendTickMs) {
                lastBackendSendMs = now;
                sendPosition(lat, lon);
            }

            if (now - lastStepMs >= stepEveryMs) {
                lastStepMs = now;
                idx++;

                // pickup je prvi stopIndex
                if (!pickupNotified && !stopIndices.isEmpty() && idx == stopIndices.get(0)) {
                    pickupNotified = true;
                    if (listener != null) listener.onPickupArrived();
                }

                if (isStopIndex(idx)) {
                    pausedUntilMs = now + pauseAtStopMs;
                }
            }

            if (idx >= path.size()) {
                double[] last = path.get(path.size() - 1);
                sendPosition(last[0], last[1]);
                if (listener != null) listener.onTick(last[0], last[1]);
                running = false;
                if (listener != null) listener.onArrived();
                return;
            }

            handler.postDelayed(this, uiTickMs);
        }
    };

    private void emitCurrentPoint(long now) {
        if (idx < 0 || idx >= path.size()) return;

        double[] p = path.get(idx);
        double lat = p[0];
        double lon = p[1];

        if (listener != null) listener.onTick(lat, lon);

        // dok stojimo, dovoljno je da backend dobije poziciju povremeno
        if (now - lastBackendSendMs >= backendTickMs) {
            lastBackendSendMs = now;
            sendPosition(lat, lon);
        }
    }


    private boolean isStopIndex(int i) {
        for (int s : stopIndices) {
            if (s == i) return true;
        }
        return false;
    }

    private void sendPosition(double lat, double lon) {
        if (bearerToken == null) return;

        vehiclesApi.updateVehiclePosition(
                "Bearer " + bearerToken,
                vehicleId,
                new VehiclePositionUpdateDto(lat, lon)
        ).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) { }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) { }
        });
    }


    private List<double[]> resampleEveryMeters(List<double[]> raw, double stepMeters) {
        List<double[]> out = new ArrayList<>();
        if (raw == null || raw.size() < 2) return out;

        out.add(new double[]{raw.get(0)[0], raw.get(0)[1]});

        double carry = 0.0;

        for (int i = 0; i < raw.size() - 1; i++) {
            double[] a = raw.get(i);
            double[] b = raw.get(i + 1);

            double segDist = distanceMeters(a[0], a[1], b[0], b[1]);
            if (segDist < 0.5) continue;

            double traveled = carry;

            while (traveled + stepMeters <= segDist) {
                traveled += stepMeters;
                double t = traveled / segDist;
                double lat = a[0] + (b[0] - a[0]) * t;
                double lon = a[1] + (b[1] - a[1]) * t;
                out.add(new double[]{lat, lon});
            }

            carry = segDist - traveled;
        }

        double[] last = raw.get(raw.size() - 1);
        out.add(new double[]{last[0], last[1]});
        return out;
    }

    private int nearestIndex(List<double[]> path, double[] stopLatLon) {
        int best = 0;
        double bestD = Double.MAX_VALUE;
        for (int i = 0; i < path.size(); i++) {
            double[] p = path.get(i);
            double d = distanceMeters(p[0], p[1], stopLatLon[0], stopLatLon[1]);
            if (d < bestD) { bestD = d; best = i; }
        }
        return best;
    }

    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
