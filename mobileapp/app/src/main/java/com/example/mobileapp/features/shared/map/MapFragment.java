package com.example.mobileapp.features.shared.map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.VehiclesApi;
import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.VehicleMarker;
import com.example.mobileapp.features.shared.repositories.UserRepository;
import com.example.mobileapp.features.unregistered.rideEstimate.EstimateBottomSheetFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment {
    private VehicleMarker lastOnlyVehicle = null;
    private long refreshIntervalMs = 5000L;
    private boolean pageReady = false;
    private List<MapFragment.RoutePoint> pendingRoute = null;
    private static final String ARG_ONLY_VEHICLE_ID = "onlyVehicleId";

    public static MapFragment newSingleVehicle(int vehicleId) {
        MapFragment f = new MapFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_ONLY_VEHICLE_ID, vehicleId);
        f.setArguments(b);
        return f;
    }

    public static MapFragment newAllVehicles() {
        return new MapFragment();
    }

    private WebView webView;
    private VehiclesApi vehiclesApi;
    private Button getEstimateButton;
    private final Gson gson = new Gson();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshTask = new Runnable() {
        @Override public void run() {
            fetchVehicles();
            handler.postDelayed(this, refreshIntervalMs);
        }
    };

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        webView = v.findViewById(R.id.mapWebView);
        getEstimateButton = v.findViewById(R.id.get_estimate_button);


        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Load Leaflet from assets
        webView.loadUrl("file:///android_asset/map/index.html");

        vehiclesApi = ApiClient.get().create(VehiclesApi.class);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_ONLY_VEHICLE_ID)) {
            refreshIntervalMs = 1000L; // 1s za single vehicle
        } else {
            refreshIntervalMs = 5000L;
        }

        webView.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN ||
                    event.getActionMasked() == android.view.MotionEvent.ACTION_MOVE) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP ||
                    event.getActionMasked() == android.view.MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                view.performClick();
            }
            return false;
        });

        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageReady = true;

                // kad se stranica ucita, odmah posalji pending rutu ako postoji
                if (pendingRoute != null) {
                    setRoutePoints(pendingRoute);
                    pendingRoute = null;
                }
            }
        });
        UserRepository userRepo = UserRepository.getInstance();
        User currentUser = userRepo.getCurrentUser().getValue();
        if (currentUser == null) {
            getEstimateButton.setVisibility(View.VISIBLE);

            getEstimateButton.setOnClickListener(view -> {
                EstimateBottomSheetFragment estimateSheet = new EstimateBottomSheetFragment();
                estimateSheet.show(getParentFragmentManager(), "estimate");
            });
        } else {
            getEstimateButton.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(refreshTask);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshTask);
    }

    private void fetchVehicles() {
        vehiclesApi.getMapVehicles().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<VehicleMarker>> call,
                                   @NonNull Response<List<VehicleMarker>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                List<VehicleMarker> vehicles = response.body();

                // ako je postavljen onlyVehicleId, postavlajm samo taj marker
                Bundle args = getArguments();
                if (args != null && args.containsKey(ARG_ONLY_VEHICLE_ID)) {
                    int onlyId = args.getInt(ARG_ONLY_VEHICLE_ID);

                    vehicles.removeIf(v -> v == null || v.id != onlyId);

                    if (!vehicles.isEmpty()) {
                        lastOnlyVehicle = vehicles.get(0);
                    }
                }

                String json = gson.toJson(vehicles);

                String escaped = json
                        .replace("\\", "\\\\")
                        .replace("'", "\\'")
                        .replace("\n", "");

                String js = "window.setVehiclesJson('" + escaped + "');";

                if (webView != null) {
                    webView.evaluateJavascript(js, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VehicleMarker>> call, @NonNull Throwable t) {
            }
        });
    }

    @Nullable
    public double[] getLastOnlyVehicleLatLon() {
        if (lastOnlyVehicle == null) return null;
        return new double[]{ lastOnlyVehicle.lat, lastOnlyVehicle.lng };
    }

    public static class RoutePoint {
        public double lat;
        public double lon;
        public String label;

        public RoutePoint(double lat, double lon, String label) {
            this.lat = lat;
            this.lon = lon;
            this.label = label;
        }
    }

    public void setRoutePoints(List<RoutePoint> points) {
        if (points == null) return;

        // ako JS nije spreman, zapamti rutu pa posalji kad se ucita
        if (!pageReady || webView == null) {
            pendingRoute = new ArrayList<>(points);
            return;
        }

        String json = gson.toJson(points);
        String escaped = json
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "");

        String js = "window.setRoutePointsJson && window.setRoutePointsJson('" + escaped + "');";
        webView.evaluateJavascript(js, null);
    }

    public void clearRouteOnMap() {
        if (!pageReady || webView == null) return;
        webView.evaluateJavascript("window.clearRoute && window.clearRoute();", null);
    }


}
