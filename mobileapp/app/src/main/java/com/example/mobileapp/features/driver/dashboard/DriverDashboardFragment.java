package com.example.mobileapp.features.driver.dashboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.GeocodingApi;
import com.example.mobileapp.features.shared.api.dto.DriverRideDto;
import com.example.mobileapp.features.shared.api.dto.GeocodeResult;
import com.example.mobileapp.features.shared.api.dto.LocationDto;
import com.example.mobileapp.features.shared.api.dto.PassengerDto;
import com.example.mobileapp.features.shared.api.dto.RideCancellationDto;
import com.example.mobileapp.features.shared.api.dto.RideDto;
import com.example.mobileapp.features.shared.api.dto.RidePanicDto;
import com.example.mobileapp.features.shared.map.MapFragment;
import com.example.mobileapp.features.shared.services.RideSimulationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DriverDashboardFragment extends Fragment {
    private Integer lastRouteRideId = null;
    private Integer lastMapVehicleId = null;
    private TextView tvCurrentRideTitle;
    private TextView tvPassengersTitle;
    private View currentRideContent;

    private RecyclerView rvWayPoints;
    private RecyclerView rvPassengers;
    private RecyclerView rvBookedRides;

    private TextView tvWaypointsLabel;
    private View cardWaypoints;

    private TextView tvCurrentStatus;
    private TextView tvCurrentRoute;

    private WaypointAdapter waypointAdapter;
    private PassengerAdapter passengerAdapter;

    private DriverDashboardAdapter bookedAdapter;
    private DriverRidesService ridesService;

    private TextView btnStartRide;

    private TextView btnPanic;
    private TextView btnStopRide;

    private com.example.mobileapp.features.shared.services.RideSimulationService sim;
    private com.example.mobileapp.features.shared.api.RidesApi ridesApi;
    private android.content.SharedPreferences prefs;
    private final android.os.Handler arrivalH = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable arrivalRunnable;
    private Integer watchingRideId = null;

    private TextView tvCurrentRideEta;
    private final android.os.Handler etaH = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable etaRunnable;
    private Integer watchingEtaRideId = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_driver_dashboard, container, false);

        tvCurrentRideTitle = v.findViewById(R.id.tvCurrentRideTitle);
        tvPassengersTitle = v.findViewById(R.id.passengersTitle);
        currentRideContent = v.findViewById(R.id.currentRideSection);

        tvWaypointsLabel = v.findViewById(R.id.tvWaypointsLabel);
        cardWaypoints = v.findViewById(R.id.cardWaypoints);

        rvWayPoints = v.findViewById(R.id.rvWaypoints);
        rvPassengers = v.findViewById(R.id.rvPassengers);
        rvBookedRides = v.findViewById(R.id.rvBookedRides);

        tvCurrentStatus = v.findViewById(R.id.tvCurrentRideStatus);
        tvCurrentRoute = v.findViewById(R.id.tvCurrentRideRoute);

        btnStartRide = v.findViewById(R.id.btnStartRide);
        btnPanic = v.findViewById(R.id.btnPanic);
        btnStopRide = v.findViewById(R.id.btnStop);

        tvCurrentRideEta = v.findViewById(R.id.tvCurrentRideEta);

        prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE);
        ridesApi = com.example.mobileapp.core.network.ApiClient.get().create(com.example.mobileapp.features.shared.api.RidesApi.class);
        sim = new com.example.mobileapp.features.shared.services.RideSimulationService();

        setupWaypoints();
        setupPassengers();
        setupBookedRides();
        setupMapChild();

        ridesService = new DriverRidesService(requireContext());

        ridesService.currentRide().observe(getViewLifecycleOwner(), this::renderCurrentRide);
        ridesService.bookedRides().observe(getViewLifecycleOwner(), this::renderBookedRides);

        ridesService.fetchRides();

        NestedScrollView scroll = v.findViewById(R.id.dashboardScroll);
        if (scroll != null) scroll.setFillViewport(true);

        return v;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupWaypoints() {
        rvWayPoints.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWayPoints.setNestedScrollingEnabled(true);

        waypointAdapter = new WaypointAdapter(new ArrayList<>());
        rvWayPoints.setAdapter(waypointAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPassengers() {
        rvPassengers.setLayoutManager(new LinearLayoutManager(requireContext()));
        passengerAdapter = new PassengerAdapter(new ArrayList<>());
        rvPassengers.setAdapter(passengerAdapter);
    }

    private void setupBookedRides() {
        rvBookedRides.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookedAdapter = new DriverDashboardAdapter(new ArrayList<>());
        rvBookedRides.setAdapter(bookedAdapter);
    }

    private void setupMapChild() {
        if (getChildFragmentManager().findFragmentById(R.id.mapContainer) == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, new MapFragment())
                    .commit();
        }
    }

    private void renderCurrentRide(@Nullable DriverRideDto r) {
        if (!isAdded()) return;
        if (waypointAdapter == null || passengerAdapter == null) return;

        if (r == null) {
            stopArrivalWatch();
            if (sim != null) sim.stop();
            tvCurrentRideTitle.setText("No Current Ride");
            tvPassengersTitle.setVisibility(View.GONE);
            currentRideContent.setVisibility(View.GONE);
            waypointAdapter.setItems(new ArrayList<>());
            passengerAdapter.setItems(new ArrayList<>());
            tvCurrentStatus.setVisibility(View.GONE);
            tvCurrentRoute.setVisibility(View.GONE);
            if (tvWaypointsLabel != null) tvWaypointsLabel.setVisibility(View.GONE);
            if (cardWaypoints != null) cardWaypoints.setVisibility(View.GONE);
            if (tvCurrentRideEta != null) tvCurrentRideEta.setVisibility(View.GONE);
            lastMapVehicleId = null;

            if (getChildFragmentManager().findFragmentById(R.id.mapContainer) != null) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mapContainer, new MapFragment())
                        .commit();
            }
            lastRouteRideId = null;

            Fragment mf = getChildFragmentManager().findFragmentById(R.id.mapContainer);
            if (mf instanceof MapFragment) {
                ((MapFragment) mf).clearRouteOnMap();
            }

            return;
        }

        tvCurrentRideTitle.setText("Current Ride");
        tvPassengersTitle.setVisibility(View.VISIBLE);
        currentRideContent.setVisibility(View.VISIBLE);

        tvCurrentStatus.setVisibility(View.VISIBLE);
        tvCurrentRoute.setVisibility(View.VISIBLE);
        if (Boolean.TRUE.equals(r.getPanicTriggered())){
            btnPanic.setText("Panic Sent");
            btnPanic.setEnabled(false);
            btnPanic.setAlpha(0.6f);
        }
        else {
            btnPanic.setText("Panic");
            btnPanic.setEnabled(true);
            btnPanic.setAlpha(1f);
        }
        if ("IN_PROGRESS".equals(r.status)) {
            startArrivalWatch(r);
            startEtaPolling(r.id);
            tvCurrentStatus.setText("Started");
            tvCurrentStatus.setBackgroundResource(R.drawable.bg_started);
            btnPanic.setVisibility(View.VISIBLE);
            btnStopRide.setVisibility(View.VISIBLE);
            setBtnWaiting();
        } else if ("ACCEPTED".equals(r.status)) {
            stopArrivalWatch();
            tvCurrentStatus.setText("Accepted");
            tvCurrentStatus.setBackgroundResource(R.drawable.bg_assigned);
            btnPanic.setVisibility(View.INVISIBLE);
            btnStopRide.setVisibility(View.INVISIBLE);
            setBtnStartIdle();
        } else {
            stopEtaPolling();
            stopArrivalWatch();
            tvCurrentStatus.setText("Scheduled");
            tvCurrentStatus.setBackgroundResource(R.drawable.bg_scheduled);
            setBtnStartIdle();
            btnStartRide.setEnabled(false);
            btnStartRide.setAlpha(0.6f);
            btnPanic.setVisibility(View.INVISIBLE);
            btnStopRide.setVisibility(View.INVISIBLE);
        }

        String from = (r.startLocation != null) ? safe(r.startLocation.getAddress()) : "";
        String to = (r.endLocation != null) ? safe(r.endLocation.getAddress()) : "";
        tvCurrentRoute.setText(from + "  →  " + to);

        boolean hasWaypoints = r.waypoints != null && !r.waypoints.isEmpty();

        if (tvWaypointsLabel != null) tvWaypointsLabel.setVisibility(hasWaypoints ? View.VISIBLE : View.GONE);
        if (cardWaypoints != null) cardWaypoints.setVisibility(hasWaypoints ? View.VISIBLE : View.GONE);

        if (!hasWaypoints) {
            waypointAdapter.setItems(new ArrayList<>());
        } else {
            List<Waypoint> wp = new ArrayList<>();
            for (LocationDto w : r.waypoints) {
                if (w == null) continue;
                wp.add(new Waypoint(safe(w.getAddress())));
            }
            waypointAdapter.setItems(wp);
        }

        List<Passenger> ps = new ArrayList<>();
        if (r.passengers != null) {
            for (PassengerDto p : r.passengers) {
                if (p == null) continue;
                String name = (safe(p.firstName) + " " + safe(p.lastName)).trim();
                String email = safe(p.email);
                ps.add(new Passenger(name.isEmpty() ? email : name, email));
            }
        }
        passengerAdapter.setItems(ps);

        btnStartRide.setOnClickListener(v -> {
            if (r == null || r.id == null || r.vehicleId == null) return;

            // Start ima smisla samo kad je ACCEPTED
            if (!"ACCEPTED".equals(r.status)) return;

            String token = prefs.getString("jwt", null);
            if (token == null || token.isEmpty()) return;

            setBtnWaiting();

            ridesApi.startRide("Bearer " + token, r.id).enqueue(new retrofit2.Callback<DriverRideDto>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<DriverRideDto> call,
                                       @NonNull retrofit2.Response<DriverRideDto> response) {
                    if (!response.isSuccessful()) {
                        setBtnStartIdle();
                        return;
                    }

                    // baseStops: pickup - waypoints - destination
                    if (r.startLocation == null || r.endLocation == null) {
                        setBtnStartIdle();
                        return;
                    }

                    final List<double[]> baseStops = new ArrayList<>();
                    baseStops.add(new double[]{r.startLocation.getLatitude(), r.startLocation.getLongitude()});

                    if (r.waypoints != null) {
                        for (LocationDto w : r.waypoints) {
                            if (w == null) continue;
                            baseStops.add(new double[]{w.getLatitude(), w.getLongitude()});
                        }
                    }

                    baseStops.add(new double[]{r.endLocation.getLatitude(), r.endLocation.getLongitude()});

                    if (baseStops.size() < 2) {
                        setBtnStartIdle();
                        return;
                    }

                    // prvo uzeti poslkednju poznatu poziciju iz MapFragment
                    double[] vehiclePos = null;
                    Fragment mf = getChildFragmentManager().findFragmentById(R.id.mapContainer);
                    if (mf instanceof MapFragment) {
                        vehiclePos = ((MapFragment) mf).getLastOnlyVehicleLatLon();
                    }

                    // ako imamo vehiclePos: extendedStops = vehicle -> pickup -> ... -> dest
                    if (vehiclePos != null) {
                        final List<double[]> extendedStops = new ArrayList<>();
                        extendedStops.add(vehiclePos);
                        extendedStops.addAll(baseStops);

                        // nacrtaj vehicle-pickup-...-dest
                        drawRoutePoints(extendedStops, true);

                        sim.startByRoute(r.vehicleId, token, extendedStops, new RideSimulationService.Listener() {
                            @Override public void onTick(double lat, double lon) { }

                            @Override public void onPickupArrived() {
                                // kad stigne do pickupa, ostavi samo pickup-...-dest
                                drawRoutePoints(baseStops, false);
                                String token = prefs.getString("jwt", null);
                                if (token != null && !token.isEmpty() && r.id != null) {
                                    ridesApi.arrivedAtPickup("Bearer " + token, r.id).enqueue(new retrofit2.Callback<>() {
                                        @Override public void onResponse(@NonNull retrofit2.Call<Void> call,
                                                                         @NonNull retrofit2.Response<Void> response) { }
                                        @Override public void onFailure(@NonNull retrofit2.Call<Void> call,
                                                                        @NonNull Throwable t) { }
                                    });
                                }
                            }

                            @Override public void onArrived() {
                                setBtnCompleteRide(r);
                            }
                        });

                        tvCurrentStatus.setText("Started");
                        tvCurrentStatus.setBackgroundResource(R.drawable.bg_started);
                        if (ridesService != null) {
                            ridesService.fetchRides();
                        }
                        return;
                    }

                    // nemamo vehiclePos - kreni od pickupa
                    drawRoutePoints(baseStops, false);

                    sim.startByRoute(r.vehicleId, token, baseStops, new RideSimulationService.Listener() {
                        @Override public void onTick(double lat, double lon) { }
                        @Override public void onPickupArrived() { }
                        @Override public void onArrived() { setBtnCompleteRide(r); }
                    });

                    tvCurrentStatus.setText("Started");
                    tvCurrentStatus.setBackgroundResource(R.drawable.bg_started);
                    if (ridesService != null) {
                        ridesService.fetchRides();
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<DriverRideDto> call,
                                      @NonNull Throwable t) {
                    setBtnStartIdle();
                }
            });
        });


        // show only assigned vehicle for this ride
        if (r.vehicleId != null) {
            if (lastMapVehicleId == null || !lastMapVehicleId.equals(r.vehicleId)) {
                lastMapVehicleId = r.vehicleId;

                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mapContainer, MapFragment.newSingleVehicle(r.vehicleId))
                        .commitNow();
            }
        } else {
            // ako backend nekad ne posalje vehicle, vrati all
            lastMapVehicleId = null;

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, new MapFragment())
                    .commit();
        }
        boolean simActive = sim != null && sim.isRunning();

        // always draw route when ride exists (ali NE dok simulacija radi)
        if (!simActive && (lastRouteRideId == null || !lastRouteRideId.equals(r.id))) {
            lastRouteRideId = r.id;

            Fragment mf = getChildFragmentManager().findFragmentById(R.id.mapContainer);
            if (mf instanceof MapFragment) {
                MapFragment mapF = (MapFragment) mf;

                ArrayList<MapFragment.RoutePoint> pts = new ArrayList<>();

                if (r.startLocation != null) {
                    pts.add(new MapFragment.RoutePoint(
                            r.startLocation.getLatitude(),
                            r.startLocation.getLongitude(),
                            "Pickup"
                    ));
                }

                if (r.waypoints != null) {
                    for (int i = 0; i < r.waypoints.size(); i++) {
                        LocationDto w = r.waypoints.get(i);
                        if (w == null) continue;
                        pts.add(new MapFragment.RoutePoint(
                                w.getLatitude(),
                                w.getLongitude(),
                                "Stop " + (i + 1)
                        ));
                    }
                }

                if (r.endLocation != null) {
                    pts.add(new MapFragment.RoutePoint(
                            r.endLocation.getLatitude(),
                            r.endLocation.getLongitude(),
                            "Destination"
                    ));
                }

                if (pts.size() >= 2) {
                    mapF.setRoutePoints(pts);
                }
            }
        }
        btnPanic.setOnClickListener(view -> {

            btnPanic.setEnabled(false);
            btnPanic.setAlpha(0.6f);
            btnPanic.setText("Sending...");
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Panic")
                    .setMessage("Are you sure you want to trigger panic for this ride?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String token = prefs.getString("jwt", null);
                        if (token == null || token.isEmpty()) {
                            btnPanic.setEnabled(true);
                            btnPanic.setAlpha(1f);
                            btnPanic.setText("Panic");
                            return;
                        }
                        int userId = prefs.getInt("userId", -1);
                        if (userId == -1) {
                            btnPanic.setEnabled(true);
                            btnPanic.setAlpha(1f);
                            btnPanic.setText("Panic");
                            return;
                        }
                        btnPanic.setEnabled(false);
                        btnPanic.setAlpha(0.6f);
                        RidePanicDto panicDto = new RidePanicDto(userId);
                        ridesApi.panic("Bearer " + token, r.id, panicDto)
                                .enqueue(new retrofit2.Callback<Void>() {
                                    @Override
                                    public void onResponse(@NonNull retrofit2.Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                                        if (!isAdded()) return;
                                        btnPanic.setEnabled(true);
                                        btnPanic.setAlpha(1f);

                                        if (response.isSuccessful()) {
                                            android.widget.Toast.makeText(requireContext(),
                                                            "Panic sent to dispatcher.",
                                                            android.widget.Toast.LENGTH_LONG).show();
                                            btnPanic.setText("Panic Sent");
                                            btnPanic.setEnabled(false);
                                            btnPanic.setAlpha(0.6f);
                                            if (ridesService != null) ridesService.fetchRides();
                                        } else {
                                            btnPanic.setEnabled(true);
                                            btnPanic.setAlpha(1f);
                                            btnPanic.setText("Panic");
                                            android.widget.Toast.makeText(requireContext(),
                                                            "Failed to send panic. Try again.",
                                                            android.widget.Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull retrofit2.Call<Void> call, @NonNull Throwable t) {
                                        if (!isAdded()) return;
                                        btnPanic.setEnabled(true);
                                        btnPanic.setAlpha(1f);
                                        btnPanic.setText("Panic");

                                        android.widget.Toast.makeText(requireContext(),
                                                        "Network error. Panic not sent.",
                                                        android.widget.Toast.LENGTH_LONG) .show();
                                    }
                                });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        btnPanic.setEnabled(true);
                        btnPanic.setAlpha(1f);
                        btnPanic.setText("Panic");
                    })
                    .setOnCancelListener(dialog -> {
                        btnPanic.setEnabled(true);
                        btnPanic.setAlpha(1f);
                        btnPanic.setText("Panic");
                    })
                    .show();
        });
        btnStopRide.setOnClickListener(view -> {
            btnStopRide.setEnabled(false);
            btnStopRide.setAlpha(0.6f);
            btnStopRide.setText("Stopping...");

            new AlertDialog.Builder(requireContext())
                    .setTitle("Stop Ride")
                    .setMessage("Are you sure you want to stop the ride here?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String token = prefs.getString("jwt", null);
                        if (token == null || token.isEmpty()) {
                            btnStopRide.setEnabled(true);
                            btnStopRide.setAlpha(1f);
                            btnStopRide.setText("Stop ride here");
                            return;
                        }

                        int userId = prefs.getInt("userId", -1);
                        if (userId == -1) {
                            btnStopRide.setEnabled(true);
                            btnStopRide.setAlpha(1f);
                            btnStopRide.setText("Stop ride here");
                            return;
                        }

                        btnStopRide.setEnabled(false);
                        btnStopRide.setAlpha(0.6f);

                        LocationDto locationDto = new LocationDto();
                        Fragment mf = getChildFragmentManager().findFragmentById(R.id.mapContainer);
                        if (mf instanceof MapFragment) {
                            double[] pos = ((MapFragment) mf).getLastOnlyVehicleLatLon();
                            if (pos != null) {
                                locationDto.setLatitude(pos[0]);
                                locationDto.setLongitude(pos[1]);
                                OkHttpClient client = new OkHttpClient.Builder()
                                        .addInterceptor(chain -> {
                                            Request original = chain.request();
                                            Request request = original.newBuilder()
                                                    .header("User-Agent", "UberPlusAndroid")
                                                    .build();
                                            return chain.proceed(request);
                                        })
                                        .connectTimeout(10, TimeUnit.SECONDS)
                                        .readTimeout(10, TimeUnit.SECONDS)
                                        .build();

                                Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl("https://nominatim.openstreetmap.org/")
                                        .client(client)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build();
                                GeocodingApi geocodeApi = retrofit.create(GeocodingApi.class);
                                geocodeApi.reverseGeocode(pos[0],pos[1]).enqueue(new Callback<GeocodeResult>() {
                                    @Override
                                    public void onResponse(Call<GeocodeResult> call, Response<GeocodeResult> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            locationDto.setAddress(response.body().getFormattedResult());
                                        } else {
                                            locationDto.setAddress("Unknown address");
                                        }
                                        ridesApi.stopEarly("Bearer " + token, r.id, locationDto)
                                                .enqueue(new Callback<RideDto>() {
                                                    @Override
                                                    public void onResponse(@NonNull Call<RideDto> call,
                                                                           @NonNull Response<RideDto> response) {
                                                        if (!isAdded()) return;

                                                        btnStopRide.setEnabled(true);
                                                        btnStopRide.setAlpha(1f);

                                                        if (response.isSuccessful()) {
                                                            Toast.makeText(requireContext(),
                                                                    "Ride stopped successfully.",
                                                                    Toast.LENGTH_LONG).show();
                                                            btnStopRide.setText("Ride Stopped");
                                                            btnStopRide.setEnabled(false);
                                                            btnStopRide.setAlpha(0.6f);
                                                            if (sim != null) {
                                                                sim.stop();
                                                            }
                                                            stopArrivalWatch();
                                                            stopEtaPolling();
                                                            if (ridesService != null) {
                                                                ridesService.fetchRides();
                                                            }
                                                        } else {
                                                            btnStopRide.setEnabled(true);
                                                            btnStopRide.setAlpha(1f);
                                                            btnStopRide.setText("Stop ride here");
                                                            Toast.makeText(requireContext(),
                                                                    "Failed to stop ride. Try again.",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull Call<RideDto> call,
                                                                          @NonNull Throwable t) {
                                                        if (!isAdded()) return;

                                                        btnStopRide.setEnabled(true);
                                                        btnStopRide.setAlpha(1f);
                                                        btnStopRide.setText("Stop ride here");
                                                        Toast.makeText(requireContext(),
                                                                "Network error. Ride not stopped.",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailure(Call<GeocodeResult> call, Throwable t) {

                                    }
                                });
                            }
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        btnStopRide.setEnabled(true);
                        btnStopRide.setAlpha(1f);
                        btnStopRide.setText("Stop ride here");
                    })
                    .setOnCancelListener(dialog -> {
                        btnStopRide.setEnabled(true);
                        btnStopRide.setAlpha(1f);
                        btnStopRide.setText("Stop ride here");
                    })
                    .show();
        });
    }

    private void renderBookedRides(@Nullable List<DriverRideDto> list) {
        List<DriverDashboardAdapter.BookedRide> mapped = new ArrayList<>();
        if (list != null) {
            for (DriverRideDto r : list) {
                if (r == null) continue;

                String date = formatDateFromIso(r.scheduledTime);
                String time = formatTimeFromIso(r.scheduledTime);

                String from = (r.startLocation != null) ? safe(r.startLocation.getAddress()) : "";
                String to = (r.endLocation != null) ? safe(r.endLocation.getAddress()) : "";

                int passengerCount = 0;
                if (r.passengers != null) passengerCount = r.passengers.size();
                else if (r.passengerEmails != null) passengerCount = r.passengerEmails.size();

                List<DriverDashboardAdapter.Requirement> reqs = new ArrayList<>();
                if (r.vehicleType != null) {
                    if ("VAN".equals(r.vehicleType)) reqs.add(DriverDashboardAdapter.Requirement.VAN);
                    else if ("STANDARD".equals(r.vehicleType)) reqs.add(DriverDashboardAdapter.Requirement.SEDAN);
                    else reqs.add(DriverDashboardAdapter.Requirement.LUXURY);
                }

                if (r.babyFriendly) reqs.add(DriverDashboardAdapter.Requirement.BABY);
                if (r.petsFriendly) reqs.add(DriverDashboardAdapter.Requirement.PETS);

                DriverDashboardAdapter.RideStatus st = DriverDashboardAdapter.RideStatus.SCHEDULED;
                if ("IN_PROGRESS".equals(r.status)) st = DriverDashboardAdapter.RideStatus.STARTED;
                else if ("ACCEPTED".equals(r.status)) st = DriverDashboardAdapter.RideStatus.ASSIGNED;

                mapped.add(new DriverDashboardAdapter.BookedRide(
                        r.id,
                        date, time,
                        from, to,
                        passengerCount,
                        reqs,
                        st
                ));
            }
        }
        bookedAdapter.setOnCancelClickListener(rideId -> {
            cancelRide(rideId);
        });
        bookedAdapter.setItems(mapped);
    }
    private void cancelRide(int rideId) {
        String token = prefs.getString("jwt", null);
        if (token == null || token.isEmpty()) {
            android.widget.Toast.makeText(requireContext(),
                    "Authentication required",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            android.widget.Toast.makeText(requireContext(),
                    "User not found",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter cancellation reason");
        input.setMinLines(3);
        input.setMaxLines(5);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        input.setPadding(50, 40, 50, 40);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cancel Ride")
                .setMessage("Please provide a reason for cancelling this ride:")
                .setView(input)
                .setPositiveButton("Cancel Ride", (dialog, which) -> {
                    String reason = input.getText().toString().trim();

                    if (reason.isEmpty()) {
                        android.widget.Toast.makeText(requireContext(),
                                "Please provide a cancellation reason",
                                android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RideCancellationDto dto = new RideCancellationDto(userId, reason);
        ridesApi.cancelRide("Bearer " + token, rideId, dto).enqueue(new Callback<RideDto>() {
            @Override
            public void onResponse(@NonNull Call<RideDto> call,
                                   @NonNull Response<RideDto> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    android.widget.Toast.makeText(requireContext(),
                            "Ride cancelled successfully",
                            android.widget.Toast.LENGTH_LONG).show();

                    if (ridesService != null) {
                        ridesService.fetchRides();
                    }
                } else {
                    android.widget.Toast.makeText(requireContext(),
                            "Failed to cancel ride",
                            android.widget.Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RideDto> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;

                android.widget.Toast.makeText(requireContext(),
                        "Network error. Please try again.",
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    })          .setNegativeButton("Keep Ride", null)
                .show();
    }
    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatMinutes(int total) {
        int h = total / 60;
        int m = total % 60;
        return String.format(Locale.getDefault(), "%dh %02dmin", h, m);
    }

    private String formatDateFromIso(String iso) {
        if (iso == null) return "--.--.";
        try {
            String s = iso;
            int dot = s.indexOf('.');
            if (dot != -1) s = s.substring(0, dot);
            String[] parts = s.split("T");
            if (parts.length == 0) return "--.--.";
            String[] ymd = parts[0].split("-");
            if (ymd.length != 3) return "--.--.";
            return ymd[2] + "." + ymd[1] + ".";
        } catch (Exception e) {
            return "--.--.";
        }
    }

    private String formatTimeFromIso(String iso) {
        if (iso == null) return "--:--";
        try {
            String s = iso;
            int dot = s.indexOf('.');
            if (dot != -1) s = s.substring(0, dot);
            String[] parts = s.split("T");
            if (parts.length != 2) return "--:--";
            String[] hms = parts[1].split(":");
            if (hms.length < 2) return "--:--";
            return hms[0] + ":" + hms[1];
        } catch (Exception e) {
            return "--:--";
        }
    }

    private static final class Passenger {
        final String name;
        final String phoneOrEmail;

        Passenger(String name, String phoneOrEmail) {
            this.name = name;
            this.phoneOrEmail = phoneOrEmail;
        }
    }

    private static final class PassengerAdapter extends RecyclerView.Adapter<PassengerAdapter.PassengerVH> {

        private final List<Passenger> items;

        PassengerAdapter(List<Passenger> items) {
            this.items = items;
        }

        void setItems(List<Passenger> newItems) {
            items.clear();
            if (newItems != null) items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PassengerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_passenger, parent, false);
            return new PassengerVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PassengerVH h, int position) {
            Passenger p = items.get(position);
            h.tvName.setText(p.name);
            h.tvPhone.setText(p.phoneOrEmail);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class PassengerVH extends RecyclerView.ViewHolder {
            final TextView tvName;
            final TextView tvPhone;

            PassengerVH(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPassengerName);
                tvPhone = itemView.findViewById(R.id.tvPassengerPhone);
            }
        }
    }

    private static final class Waypoint {
        final String address;

        Waypoint(String address) {
            this.address = address;
        }
    }

    private static final class WaypointAdapter extends RecyclerView.Adapter<WaypointAdapter.WaypointVH> {

        private final List<Waypoint> items;

        WaypointAdapter(List<Waypoint> items) {
            this.items = items;
        }

        void setItems(List<Waypoint> newItems) {
            items.clear();
            if (newItems != null) items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public WaypointVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_waypoint, parent, false);
            return new WaypointVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull WaypointVH h, int position) {
            Waypoint w = items.get(position);
            h.tvAddress.setText(w.address);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class WaypointVH extends RecyclerView.ViewHolder {
            final TextView tvAddress;

            WaypointVH(@NonNull View itemView) {
                super(itemView);
                tvAddress = itemView.findViewById(R.id.tvWaypointAddress);
            }
        }
    }
    private void setBtnStartIdle() {
        if (btnStartRide == null) return;
        btnStartRide.setText("Start Ride");
        btnStartRide.setEnabled(true);
        btnStartRide.setAlpha(1f);
    }

    private void setBtnWaiting() {
        if (btnStartRide == null) return;
        btnStartRide.setText("Waiting for arrival");
        btnStartRide.setEnabled(false);
        btnStartRide.setAlpha(0.6f);
    }

    private void setBtnCompleteRide(@NonNull DriverRideDto r) {
        if (btnStartRide == null) return;

        btnStartRide.setText("Complete Ride");
        btnStartRide.setEnabled(true);
        btnStartRide.setAlpha(1f);

        btnStartRide.setOnClickListener(v -> {
            if (r.id == null) return;

            String token = prefs.getString("jwt", null);
            if (token == null || token.isEmpty()) return;

            btnStartRide.setEnabled(false);
            btnStartRide.setAlpha(0.6f);
            btnStartRide.setText("Completing...");

            if (sim != null) sim.stop();

            ridesApi.completeRide("Bearer " + token, r.id).enqueue(new retrofit2.Callback<DriverRideDto>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<DriverRideDto> call,
                                       @NonNull retrofit2.Response<DriverRideDto> response) {
                    if (!response.isSuccessful()) {
                        btnStartRide.setEnabled(true);
                        btnStartRide.setAlpha(1f);
                        btnStartRide.setText("Complete Ride");
                        return;
                    }
                    btnStartRide.setText("Refreshing...");
                    btnStartRide.setEnabled(false);
                    btnStartRide.setAlpha(0.6f);
                    if (ridesService != null) ridesService.fetchRides();
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<DriverRideDto> call,
                                      @NonNull Throwable t) {
                    btnStartRide.setEnabled(true);
                    btnStartRide.setAlpha(1f);
                    btnStartRide.setText("Complete Ride");
                }
            });
        });
    }


    private void drawRoutePoints(@NonNull List<double[]> stopsLatLon, boolean hasVehicle) {
        if (!isAdded() || isDetached() || getActivity() == null) return;
        if (getView() == null) return;
        Fragment mf = getChildFragmentManager().findFragmentById(R.id.mapContainer);
        if (!(mf instanceof MapFragment)) return;

        MapFragment mapF = (MapFragment) mf;

        ArrayList<MapFragment.RoutePoint> pts = new ArrayList<>();

        for (int i = 0; i < stopsLatLon.size(); i++) {
            double[] p = stopsLatLon.get(i);

            String label;
            if (hasVehicle) {
                if (i == 0) label = "Vehicle";
                else if (i == 1) label = "Pickup";
                else if (i == stopsLatLon.size() - 1) label = "Destination";
                else label = "Stop " + (i - 1);
            } else {
                if (i == 0) label = "Pickup";
                else if (i == stopsLatLon.size() - 1) label = "Destination";
                else label = "Stop " + i;
            }

            pts.add(new MapFragment.RoutePoint(p[0], p[1], label));
        }

        if (pts.size() >= 2) mapF.setRoutePoints(pts);
    }
    private void stopArrivalWatch() {
        if (arrivalRunnable != null) arrivalH.removeCallbacks(arrivalRunnable);
        arrivalRunnable = null;
        watchingRideId = null;
    }

    private void startArrivalWatch(@NonNull DriverRideDto r) {
        if (r.id == null || r.endLocation == null) return;
        if (watchingRideId != null && watchingRideId.equals(r.id)) return;

        stopArrivalWatch();
        watchingRideId = r.id;

        arrivalRunnable = new Runnable() {
            @Override public void run() {
                if (!isAdded()) return;
                // ako je voznja promijenjena u medjuvremenu, prekini
                DriverRideDto cur = ridesService != null ? ridesService.currentRide().getValue() : null;
                if (cur == null || cur.id == null || !cur.id.equals(r.id) || !"IN_PROGRESS".equals(cur.status)) {
                    stopArrivalWatch();
                    return;
                }

                Fragment mf = getChildFragmentManager().findFragmentById(R.id.mapContainer);
                double[] pos = (mf instanceof MapFragment) ? ((MapFragment) mf).getLastOnlyVehicleLatLon() : null;

                if (pos != null) {
                    double dist = RideSimulationService.distanceMeters(pos[0], pos[1],
                            cur.endLocation.getLatitude(), cur.endLocation.getLongitude());
                    if (dist <= 30.0) {
                        setBtnCompleteRide(cur);
                        stopArrivalWatch();
                        return;
                    } else {
                        // dok se ne stigne, mozemo prikazati waiting
                        setBtnWaiting();
                    }
                }

                arrivalH.postDelayed(this, 800);
            }
        };

        arrivalH.post(arrivalRunnable);
    }

    private static String formatEta(Integer seconds) {
        if (seconds == null) return "--";
        int s = Math.max(0, seconds);
        int min = s / 60;
        if (min <= 0) return "< 1 min";
        return min + " min";
    }

    private void stopEtaPolling() {
        if (etaRunnable != null) etaH.removeCallbacks(etaRunnable);
        etaRunnable = null;
        watchingEtaRideId = null;
    }

    private void startEtaPolling(int rideId) {
        if (watchingEtaRideId != null && watchingEtaRideId.equals(rideId)) return;
        stopEtaPolling();
        watchingEtaRideId = rideId;

        etaRunnable = new Runnable() {
            @Override public void run() {
                if (!isAdded()) return;

                DriverRideDto cur = ridesService != null ? ridesService.currentRide().getValue() : null;
                if (cur == null || cur.id == null || !cur.id.equals(rideId) || !"IN_PROGRESS".equals(cur.status)) {
                    stopEtaPolling();
                    return;
                }

                String token = prefs.getString("jwt", null);
                if (token == null || token.isEmpty()) { stopEtaPolling(); return; }

                ridesApi.getRideEta("Bearer " + token, rideId).enqueue(new retrofit2.Callback<>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<com.example.mobileapp.features.shared.api.dto.RideEtaDto> call,
                                           @NonNull retrofit2.Response<com.example.mobileapp.features.shared.api.dto.RideEtaDto> resp) {
                        if (!isAdded()) return;

                        if (!resp.isSuccessful() || resp.body() == null) {
                            if (tvCurrentRideEta != null) tvCurrentRideEta.setText("ETA: --");
                            etaH.postDelayed(etaRunnable, 2000);
                            return;
                        }

                        var eta = resp.body();

                        String label;
                        if ("TO_PICKUP".equals(eta.phase)) label = "ETA to pickup: " + formatEta(eta.etaToNextPointSeconds);
                        else label = "ETA to destination: " + formatEta(eta.etaToNextPointSeconds);

                        if (tvCurrentRideEta != null) tvCurrentRideEta.setText(label);

                        etaH.postDelayed(etaRunnable, 2000);
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<com.example.mobileapp.features.shared.api.dto.RideEtaDto> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        if (tvCurrentRideEta != null) tvCurrentRideEta.setText("ETA: --");
                        etaH.postDelayed(etaRunnable, 2000);
                    }
                });
            }
        };

        etaH.post(etaRunnable);
    }

    @Override
    public void onDestroyView() {
        stopEtaPolling();
        stopArrivalWatch();
        super.onDestroyView();
    }
}
