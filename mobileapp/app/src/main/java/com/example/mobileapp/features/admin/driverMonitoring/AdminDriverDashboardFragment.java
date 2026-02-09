package com.example.mobileapp.features.admin.driverMonitoring;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.admin.services.AdminDriverRidesService;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.dto.DriverRideDto;
import com.example.mobileapp.features.shared.api.dto.LocationDto;
import com.example.mobileapp.features.shared.api.dto.PassengerDto;
import com.example.mobileapp.features.shared.map.MapFragment;
import com.example.mobileapp.features.shared.services.RideSimulationService;

import java.util.ArrayList;
import java.util.List;

public class AdminDriverDashboardFragment extends Fragment {

    private static final String ARG_DRIVER_EMAIL = "driver_email";

    private String driverEmail;
    private AdminDriverRidesService ridesService;
    private RidesApi ridesApi;
    private SharedPreferences prefs;

    private TextView tvStatus;
    private TextView tvRoute;
    private TextView tvEta;
    private RecyclerView rvWaypoints;
    private RecyclerView rvPassengers;
    private View layoutWaypoints;

    private WaypointAdapter waypointAdapter;
    private PassengerAdapter passengerAdapter;

    private final Handler etaHandler = new Handler(Looper.getMainLooper());
    private Runnable etaRunnable;
    private Integer watchingRideId = null;

    public static AdminDriverDashboardFragment newInstance(String driverEmail) {
        AdminDriverDashboardFragment f = new AdminDriverDashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DRIVER_EMAIL, driverEmail);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            driverEmail = getArguments().getString(ARG_DRIVER_EMAIL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_driver_dashboard, container, false);

        tvStatus = v.findViewById(R.id.tvRideStatus);
        tvRoute = v.findViewById(R.id.tvRideRoute);
        tvEta = v.findViewById(R.id.tvRideEta);
        rvWaypoints = v.findViewById(R.id.rvWaypoints);
        rvPassengers = v.findViewById(R.id.rvPassengers);
        layoutWaypoints = v.findViewById(R.id.layoutWaypoints);

        setupRecyclerViews();
        setupMapChild();

        prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE);
        ridesApi = com.example.mobileapp.core.network.ApiClient.get()
                .create(com.example.mobileapp.features.shared.api.RidesApi.class);

        ridesService = new AdminDriverRidesService(requireContext());
        ridesService.currentRide().observe(getViewLifecycleOwner(), this::renderCurrentRide);

        if (driverEmail != null) {
            ridesService.fetchRidesForDriver(driverEmail);
        }

        return v;
    }

    private void setupRecyclerViews() {
        rvWaypoints.setLayoutManager(new LinearLayoutManager(requireContext()));
        waypointAdapter = new WaypointAdapter(new ArrayList<>());
        rvWaypoints.setAdapter(waypointAdapter);

        rvPassengers.setLayoutManager(new LinearLayoutManager(requireContext()));
        passengerAdapter = new PassengerAdapter(new ArrayList<>());
        rvPassengers.setAdapter(passengerAdapter);
    }

    private void setupMapChild() {
        if (getChildFragmentManager().findFragmentById(R.id.mapContainer) == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, new MapFragment())
                    .commit();
        }
    }

    @SuppressLint("SetTextI18n")
    private void renderCurrentRide(@Nullable DriverRideDto r) {
        if (!isAdded()) return;

        if (r == null) {
            stopEtaPolling();
            tvStatus.setText("No current ride");
            tvRoute.setText("");
            tvEta.setVisibility(View.GONE);
            layoutWaypoints.setVisibility(View.GONE);
            waypointAdapter.setItems(new ArrayList<>());
            passengerAdapter.setItems(new ArrayList<>());
            return;
        }

        // Status
        if ("IN_PROGRESS".equals(r.status)) {
            tvStatus.setText("In Progress");
            tvStatus.setBackgroundResource(R.drawable.bg_started);
            startEtaPolling(r.id);
            tvEta.setVisibility(View.VISIBLE);
        } else if ("ACCEPTED".equals(r.status)) {
            tvStatus.setText("Accepted");
            tvStatus.setBackgroundResource(R.drawable.bg_assigned);
            stopEtaPolling();
            tvEta.setVisibility(View.GONE);
        } else {
            tvStatus.setText("Scheduled");
            tvStatus.setBackgroundResource(R.drawable.bg_scheduled);
            stopEtaPolling();
            tvEta.setVisibility(View.GONE);
        }

        // Route
        String from = (r.startLocation != null) ? safe(r.startLocation.getAddress()) : "";
        String to = (r.endLocation != null) ? safe(r.endLocation.getAddress()) : "";
        tvRoute.setText(from + " → " + to);

        // Waypoints
        boolean hasWaypoints = r.waypoints != null && !r.waypoints.isEmpty();
        layoutWaypoints.setVisibility(hasWaypoints ? View.VISIBLE : View.GONE);

        if (hasWaypoints) {
            List<Waypoint> wp = new ArrayList<>();
            for (LocationDto w : r.waypoints) {
                if (w == null) continue;
                wp.add(new Waypoint(safe(w.getAddress())));
            }
            waypointAdapter.setItems(wp);
        } else {
            waypointAdapter.setItems(new ArrayList<>());
        }

        // Passengers
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

        // Map
        if (r.vehicleId != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, MapFragment.newSingleVehicle(r.vehicleId))
                    .commitNow();
        }

        // Route points on map
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

    private void startEtaPolling(int rideId) {
        if (watchingRideId != null && watchingRideId.equals(rideId)) return;
        stopEtaPolling();
        watchingRideId = rideId;

        etaRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;

                DriverRideDto cur = ridesService.currentRide().getValue();
                if (cur == null || cur.id == null || !cur.id.equals(rideId) || !"IN_PROGRESS".equals(cur.status)) {
                    stopEtaPolling();
                    return;
                }

                String token = prefs.getString("jwt", null);
                if (token == null || token.isEmpty()) {
                    stopEtaPolling();
                    return;
                }

                ridesApi.getRideEta("Bearer " + token, rideId).enqueue(new retrofit2.Callback<>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<com.example.mobileapp.features.shared.api.dto.RideEtaDto> call,
                                           @NonNull retrofit2.Response<com.example.mobileapp.features.shared.api.dto.RideEtaDto> resp) {
                        if (!isAdded()) return;

                        if (!resp.isSuccessful() || resp.body() == null) {
                            tvEta.setText("ETA: --");
                            etaHandler.postDelayed(etaRunnable, 2000);
                            return;
                        }

                        var eta = resp.body();
                        String label;
                        if ("TO_PICKUP".equals(eta.phase)) {
                            label = "ETA to pickup: " + formatEta(eta.etaToNextPointSeconds);
                        } else {
                            label = "ETA to destination: " + formatEta(eta.etaToNextPointSeconds);
                        }

                        tvEta.setText(label);
                        etaHandler.postDelayed(etaRunnable, 2000);
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<com.example.mobileapp.features.shared.api.dto.RideEtaDto> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        tvEta.setText("ETA: --");
                        etaHandler.postDelayed(etaRunnable, 2000);
                    }
                });
            }
        };

        etaHandler.post(etaRunnable);
    }

    private void stopEtaPolling() {
        if (etaRunnable != null) etaHandler.removeCallbacks(etaRunnable);
        etaRunnable = null;
        watchingRideId = null;
    }

    private static String formatEta(Integer seconds) {
        if (seconds == null) return "--";
        int s = Math.max(0, seconds);
        int min = s / 60;
        if (min <= 0) return "< 1 min";
        return min + " min";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onDestroyView() {
        stopEtaPolling();
        super.onDestroyView();
    }

    // Waypoint & Passenger adapters
    private static final class Waypoint {
        final String address;
        Waypoint(String address) { this.address = address; }
    }

    private static final class Passenger {
        final String name;
        final String phoneOrEmail;
        Passenger(String name, String phoneOrEmail) {
            this.name = name;
            this.phoneOrEmail = phoneOrEmail;
        }
    }

    private static final class WaypointAdapter extends RecyclerView.Adapter<WaypointAdapter.WaypointVH> {
        private final List<Waypoint> items;

        WaypointAdapter(List<Waypoint> items) { this.items = items; }

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
            h.tvAddress.setText(items.get(position).address);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static final class WaypointVH extends RecyclerView.ViewHolder {
            final TextView tvAddress;
            WaypointVH(@NonNull View itemView) {
                super(itemView);
                tvAddress = itemView.findViewById(R.id.tvWaypointAddress);
            }
        }
    }

    private static final class PassengerAdapter extends RecyclerView.Adapter<PassengerAdapter.PassengerVH> {
        private final List<Passenger> items;

        PassengerAdapter(List<Passenger> items) { this.items = items; }

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
        public int getItemCount() { return items.size(); }

        static final class PassengerVH extends RecyclerView.ViewHolder {
            final TextView tvName, tvPhone;
            PassengerVH(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPassengerName);
                tvPhone = itemView.findViewById(R.id.tvPassengerPhone);
            }
        }
    }
}