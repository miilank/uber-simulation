package com.example.mobileapp.features.passenger.currentride;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.LocationDto;
import com.example.mobileapp.features.shared.api.dto.PassengerDto;
import com.example.mobileapp.features.shared.api.dto.PassengerRideDto;
import com.example.mobileapp.features.shared.map.MapFragment;
import com.example.mobileapp.features.shared.models.PassengerItem;

import java.util.ArrayList;
import java.util.List;

public class CurrentRideFragment extends Fragment {

    // UI
    private TextView btnOpenRating;
    private Integer currentRideId = null;

    private View noCurrentRideRoot;
    private View currentRideContentRoot;

    private android.widget.TextView tvRideStatus;
    private android.widget.TextView tvRoute;
    private android.widget.TextView tvVehicle;

    private android.widget.TextView tvCharCount;
    private android.widget.TextView tvGuard;
    private android.widget.TextView btnSubmit;
    private android.widget.EditText etReportNote;

    private RecyclerView rvPassengers;

    // state
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean submitting = false;

    private PassengerCurrentRideService rideService;

    private Integer lastMapVehicleId = null;
    private Integer lastRouteRideId = null;

    private TextView tvEta;
    private final Handler etaH = new Handler(Looper.getMainLooper());
    private Runnable etaRunnable;
    private com.example.mobileapp.features.shared.api.RidesApi ridesApi;
    private SharedPreferences prefs;
    private Integer watchingEtaRideId = null;


    public CurrentRideFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        currentRideId = null;
        btnOpenRating.setEnabled(false);
        btnOpenRating.setAlpha(0.6f);
        if (btnOpenRating != null) btnOpenRating.setOnClickListener(v -> openRating());
        setupPassengers();
        setupReport();
        setupMapChild();

        rideService = new PassengerCurrentRideService(requireContext());
        rideService.currentRide().observe(getViewLifecycleOwner(), this::renderRide);
        prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE);
        ridesApi = com.example.mobileapp.core.network.ApiClient.get()
                .create(com.example.mobileapp.features.shared.api.RidesApi.class);

        rideService.fetchCurrentRide();
    }

    private void bindViews(@NonNull View view) {
        noCurrentRideRoot = view.findViewById(R.id.noCurrentRideRoot);
        currentRideContentRoot = view.findViewById(R.id.currentRideContentRoot);

        tvRideStatus = view.findViewById(R.id.tvRideStatus);
        tvRoute = view.findViewById(R.id.tvRoute);
        tvVehicle = view.findViewById(R.id.tvVehicle);

        tvCharCount = view.findViewById(R.id.tvCharCount);
        tvGuard = view.findViewById(R.id.tvGuard);
        btnSubmit = view.findViewById(R.id.btnSubmitReport);
        etReportNote = view.findViewById(R.id.etReportNote);

        rvPassengers = view.findViewById(R.id.rvPassengers);
        btnOpenRating = view.findViewById(R.id.btnOpenRating);

        tvEta = view.findViewById(R.id.tvEta);
    }

    private void setupPassengers() {
        rvPassengers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPassengers.setAdapter(new PassengerCurrentRideAdapter(new ArrayList<>()));
    }

    private void setupMapChild() {
        if (getChildFragmentManager().findFragmentById(R.id.mapContainer) == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, MapFragment.newAllVehicles())
                    .commit();
        }
    }

    private void setupReport() {
        updateCharCount();
        updateSubmitState(false);

        etReportNote.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharCount();
                updateSubmitState(true);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void renderRide(@Nullable PassengerRideDto r) {
        if (!isAdded()) return;

        if (r == null) {
            showNoCurrentRide();
            clearMapState();
            return;
        }

        if (r.status == null || !"IN_PROGRESS".equals(r.status)) {
            showNoCurrentRide();
            clearMapState();
            return;
        }

        showRideContent();

        applyStartedStyle();

        tvEta.setText("--");

        String from = (r.startLocation != null) ? safe(r.startLocation.getAddress()) : "";
        String to = (r.endLocation != null) ? safe(r.endLocation.getAddress()) : "";
        tvRoute.setText(from + " → " + to);

        String vehicleText =
                (safe(r.vehicleModel).isEmpty() || safe(r.vehicleLicensePlate).isEmpty())
                        ? "Vehicle"
                        : (safe(r.vehicleModel) + " • " + safe(r.vehicleLicensePlate));
        tvVehicle.setText(vehicleText);

        // passengers list
        List<PassengerItem> items = new ArrayList<>();
        if (r.passengers != null) {
            int idx = 1;
            for (PassengerDto p : r.passengers) {
                if (p == null) continue;
                String name = (safe(p.firstName) + " " + safe(p.lastName)).trim();
                if (name.isEmpty()) name = safe(p.email);
                items.add(new PassengerItem(idx++, name, "Passenger"));
            }
        }
        setPassengers(items);

        refreshActiveGuard(true);

        if (r.vehicleId != null) {
            if (lastMapVehicleId == null || !lastMapVehicleId.equals(r.vehicleId)) {
                lastMapVehicleId = r.vehicleId;
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mapContainer, MapFragment.newSingleVehicle(r.vehicleId))
                        .commitNow();
            }
        } else {
            lastMapVehicleId = null;
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, MapFragment.newAllVehicles())
                    .commit();
        }

        if (lastRouteRideId == null || !lastRouteRideId.equals(r.id)) {
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

                if (pts.size() >= 2) mapF.setRoutePoints(pts);
                else mapF.clearRouteOnMap();
            }
        }

        currentRideId = r.id;
        startEtaPolling(r.id);
        if (btnOpenRating != null) {
            btnOpenRating.setEnabled(true);
            btnOpenRating.setAlpha(1f);
        }
    }

    private void setPassengers(@NonNull List<PassengerItem> items) {
        RecyclerView.Adapter<?> adapter = rvPassengers.getAdapter();
        if (adapter instanceof PassengerCurrentRideAdapter) {
            ((PassengerCurrentRideAdapter) adapter).setItems(items);
        } else {
            rvPassengers.setAdapter(new PassengerCurrentRideAdapter(items));
        }
    }

    private void showNoCurrentRide() {
        if (noCurrentRideRoot != null) noCurrentRideRoot.setVisibility(View.VISIBLE);
        if (currentRideContentRoot != null) currentRideContentRoot.setVisibility(View.GONE);
        currentRideId = null;
        if (btnOpenRating != null) {
            btnOpenRating.setEnabled(false);
            btnOpenRating.setAlpha(0.6f);
        }
        stopEtaPolling();
        refreshActiveGuard(false);
    }

    private void showRideContent() {
        if (noCurrentRideRoot != null) noCurrentRideRoot.setVisibility(View.GONE);
        if (currentRideContentRoot != null) currentRideContentRoot.setVisibility(View.VISIBLE);
    }

    private void clearMapState() {
        lastMapVehicleId = null;
        lastRouteRideId = null;

        Fragment mf = getChildFragmentManager().findFragmentById(R.id.mapContainer);
        if (mf instanceof MapFragment) ((MapFragment) mf).clearRouteOnMap();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.mapContainer, MapFragment.newAllVehicles())
                .commit();
    }

    @SuppressLint("SetTextI18n")
    private void applyStartedStyle() {
        tvRideStatus.setText("Started");
        tvRideStatus.setBackgroundResource(R.drawable.bg_started);
        tvRideStatus.setTextColor(0xFF065F46);
    }

    private void refreshActiveGuard(boolean rideActive) {
        if (!rideActive) {
            if (tvGuard != null) tvGuard.setVisibility(View.VISIBLE);
            if (etReportNote != null) etReportNote.setEnabled(false);
            if (btnSubmit != null) {
                btnSubmit.setEnabled(false);
                btnSubmit.setAlpha(0.5f);
            }
        } else {
            if (tvGuard != null) tvGuard.setVisibility(View.GONE);
            if (etReportNote != null) etReportNote.setEnabled(true);
            updateSubmitState(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateCharCount() {
        String txt = (etReportNote.getText() == null) ? "" : etReportNote.getText().toString();
        tvCharCount.setText(txt.length() + "/300");
    }

    private void updateSubmitState(boolean rideActive) {
        if (!rideActive || submitting) {
            btnSubmit.setEnabled(false);
            btnSubmit.setAlpha(0.5f);
            return;
        }

        String note = (etReportNote.getText() == null) ? "" : etReportNote.getText().toString().trim();
        boolean enabled = !note.isEmpty();
        btnSubmit.setEnabled(enabled);
        btnSubmit.setAlpha(enabled ? 1f : 0.5f);
    }

    @SuppressLint("SetTextI18n")
    private void submitReport() {
        String note = (etReportNote.getText() == null) ? "" : etReportNote.getText().toString().trim();
        if (note.isEmpty()) return;

        if (currentRideId == null) {
            Toast.makeText(requireContext(), "No active ride.", Toast.LENGTH_SHORT).show();
            return;
        }

        submitting = true;
        btnSubmit.setText("Sending...");
        updateSubmitState(true);

        rideService.reportInconsistency(currentRideId, note, new PassengerCurrentRideService.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                submitting = false;
                if (etReportNote.getText() != null) etReportNote.getText().clear();
                btnSubmit.setText("Submit");
                updateCharCount();
                updateSubmitState(true);

                Toast.makeText(requireContext(), "Report sent.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull String message) {
                if (!isAdded()) return;

                submitting = false;
                btnSubmit.setText("Submit");
                updateCharCount();
                updateSubmitState(true);

                Toast.makeText(requireContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void openRating() {
        if (currentRideId == null) return;

        RatingDialogFragment dialog = RatingDialogFragment.newInstance(currentRideId);
        dialog.setListener(() -> {
            if (isAdded()) {
                android.widget.Toast.makeText(requireContext(), "Thanks! Rating saved.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getChildFragmentManager(), "rating_dialog");
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
        if (watchingEtaRideId != null && watchingEtaRideId == rideId) return;
        stopEtaPolling();
        watchingEtaRideId = rideId;

        etaRunnable = new Runnable() {
            @Override public void run() {
                if (!isAdded()) return;

                String token = prefs.getString("jwt", null);
                if (token == null || token.isEmpty()) { stopEtaPolling(); return; }

                if (currentRideId == null || currentRideId != rideId) { stopEtaPolling(); return; }

                ridesApi.getRideEta("Bearer " + token, rideId).enqueue(new retrofit2.Callback<>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<com.example.mobileapp.features.shared.api.dto.RideEtaDto> call,
                                           @NonNull retrofit2.Response<com.example.mobileapp.features.shared.api.dto.RideEtaDto> resp) {
                        if (!isAdded()) return;

                        if (!resp.isSuccessful() || resp.body() == null) {
                            tvEta.setText("--");
                            etaH.postDelayed(etaRunnable, 2000);
                            return;
                        }

                        var eta = resp.body();
                        String label = "ETA: " + formatEta(eta.etaToNextPointSeconds);

                        if ("TO_PICKUP".equals(eta.phase)) label = "ETA to pickup: " + formatEta(eta.etaToNextPointSeconds);
                        else if ("IN_PROGRESS".equals(eta.phase)) label = "ETA to destination: " + formatEta(eta.etaToNextPointSeconds);

                        tvEta.setText(label);

                        etaH.postDelayed(etaRunnable, 2000);
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<com.example.mobileapp.features.shared.api.dto.RideEtaDto> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        tvEta.setText("--");
                        etaH.postDelayed(etaRunnable, 2000);
                    }
                });
            }
        };

        etaH.post(etaRunnable);
    }
}
