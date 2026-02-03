package com.example.mobileapp.features.driver.dashboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.DriverRideDto;
import com.example.mobileapp.features.shared.api.dto.LocationDto;
import com.example.mobileapp.features.shared.api.dto.PassengerDto;
import com.example.mobileapp.features.shared.map.MapFragment;
import com.example.mobileapp.features.shared.services.RideSimulationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverDashboardFragment extends Fragment {
    private Integer lastRouteRideId = null;
    private Integer lastMapVehicleId = null;
    private TextView tvCurrentRideTitle;
    private TextView tvPassengersTitle;
    private View currentRideContent;

    private RecyclerView rvWayPoints;
    private RecyclerView rvPassengers;
    private RecyclerView rvBookedRides;

    private ProgressBar pbWork;
    private TextView tvWaypointsLabel;
    private View cardWaypoints;
    private TextView tvWorkActive;
    private TextView tvWorkLimit;

    private TextView tvCurrentStatus;
    private TextView tvCurrentRoute;

    private WaypointAdapter waypointAdapter;
    private PassengerAdapter passengerAdapter;

    private DriverDashboardAdapter bookedAdapter;
    private DriverRidesService ridesService;

    private TextView btnStartRide;

    private com.example.mobileapp.features.shared.services.RideSimulationService sim;
    private com.example.mobileapp.features.shared.api.RidesApi ridesApi;
    private android.content.SharedPreferences prefs;

    private final int workMinutes = 265;
    private final int workLimitMinutes = 480;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_driver_dashboard, container, false);

        tvCurrentRideTitle = v.findViewById(R.id.tvCurrentRideTitle);
        tvPassengersTitle = v.findViewById(R.id.passengersTitle);
        currentRideContent = v.findViewById(R.id.currentRideSection);

        pbWork = v.findViewById(R.id.pbWork);
        tvWaypointsLabel = v.findViewById(R.id.tvWaypointsLabel);
        cardWaypoints = v.findViewById(R.id.cardWaypoints);
        tvWorkActive = v.findViewById(R.id.tvWorkActive);
        tvWorkLimit = v.findViewById(R.id.tvWorkLimit);

        rvWayPoints = v.findViewById(R.id.rvWaypoints);
        rvPassengers = v.findViewById(R.id.rvPassengers);
        rvBookedRides = v.findViewById(R.id.rvBookedRides);

        tvCurrentStatus = v.findViewById(R.id.tvCurrentRideStatus);
        tvCurrentRoute = v.findViewById(R.id.tvCurrentRideRoute);

        btnStartRide = v.findViewById(R.id.btnStartRide);

        prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE);
        ridesApi = com.example.mobileapp.core.network.ApiClient.get().create(com.example.mobileapp.features.shared.api.RidesApi.class);
        sim = new com.example.mobileapp.features.shared.services.RideSimulationService();

        setupWorkingHours();
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

    private void setupWorkingHours() {
        tvWorkActive.setText(formatMinutes(workMinutes));
        tvWorkLimit.setText(String.format(Locale.getDefault(), "%s / %s",
                formatMinutes(workMinutes),
                formatMinutes(workLimitMinutes)));

        int percent = (int) Math.round((workMinutes * 100.0) / workLimitMinutes);
        percent = Math.max(0, Math.min(100, percent));
        pbWork.setProgress(percent);
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
            tvCurrentRideTitle.setText("No Current Ride");
            tvPassengersTitle.setVisibility(View.GONE);
            currentRideContent.setVisibility(View.GONE);
            waypointAdapter.setItems(new ArrayList<>());
            passengerAdapter.setItems(new ArrayList<>());
            tvCurrentStatus.setVisibility(View.GONE);
            tvCurrentRoute.setVisibility(View.GONE);

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

        if ("IN_PROGRESS".equals(r.status)) {
            tvCurrentStatus.setText("Started");
            tvCurrentStatus.setBackgroundResource(R.drawable.bg_started);
        } else if ("ACCEPTED".equals(r.status)) {
            tvCurrentStatus.setText("Accepted");
            tvCurrentStatus.setBackgroundResource(R.drawable.bg_assigned);
        } else {
            tvCurrentStatus.setText("Scheduled");
            tvCurrentStatus.setBackgroundResource(R.drawable.bg_scheduled);
        }

        String from = (r.startLocation != null) ? safe(r.startLocation.address) : "";
        String to = (r.endLocation != null) ? safe(r.endLocation.address) : "";
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
                wp.add(new Waypoint(safe(w.address)));
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

            // start ima smisla samo kad je ASSIGNED
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

                    // build points
                    List<double[]> pts = new ArrayList<>();
                    if (r.startLocation != null) pts.add(new double[]{r.startLocation.latitude, r.startLocation.longitude});
                    if (r.waypoints != null) {
                        for (LocationDto w : r.waypoints) {
                            if (w == null) continue;
                            pts.add(new double[]{w.latitude, w.longitude});
                        }
                    }
                    if (r.endLocation != null) pts.add(new double[]{r.endLocation.latitude, r.endLocation.longitude});

                    if (pts.size() < 2) {
                        setBtnStartIdle();
                        return;
                    }

                    List<double[]> stops = new ArrayList<>();
                    stops.add(new double[]{r.startLocation.latitude, r.startLocation.longitude});
                    for (LocationDto w : r.waypoints) stops.add(new double[]{w.latitude, w.longitude});
                    stops.add(new double[]{r.endLocation.latitude, r.endLocation.longitude});

                    sim.startByRoute(r.vehicleId, token, stops, new RideSimulationService.Listener() {
                        @Override public void onTick(double lat, double lon) {}
                        @Override public void onArrived() { setBtnStopNoOp(); }
                    });


                    tvCurrentStatus.setText("Started");
                    tvCurrentStatus.setBackgroundResource(R.drawable.bg_started);
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
        // always draw route when ride exists
        if (lastRouteRideId == null || !lastRouteRideId.equals(r.id)) {
            lastRouteRideId = r.id;

            Fragment mf = getChildFragmentManager().findFragmentById(R.id.mapContainer);
            if (mf instanceof MapFragment) {
                MapFragment mapF = (MapFragment) mf;

                ArrayList<MapFragment.RoutePoint> pts = new ArrayList<>();

                if (r.startLocation != null) {
                    pts.add(new MapFragment.RoutePoint(
                            r.startLocation.latitude,
                            r.startLocation.longitude,
                            "Pickup"
                    ));
                }

                if (r.waypoints != null) {
                    for (int i = 0; i < r.waypoints.size(); i++) {
                        LocationDto w = r.waypoints.get(i);
                        if (w == null) continue;
                        pts.add(new MapFragment.RoutePoint(
                                w.latitude,
                                w.longitude,
                                "Stop " + (i + 1)
                        ));
                    }
                }

                if (r.endLocation != null) {
                    pts.add(new MapFragment.RoutePoint(
                            r.endLocation.latitude,
                            r.endLocation.longitude,
                            "Destination"
                    ));
                }

                if (pts.size() >= 2) {
                    mapF.setRoutePoints(pts);
                }
            }
        }

    }

    private void renderBookedRides(@Nullable List<DriverRideDto> list) {
        List<DriverDashboardAdapter.BookedRide> mapped = new ArrayList<>();
        if (list != null) {
            for (DriverRideDto r : list) {
                if (r == null) continue;

                String date = formatDateFromIso(r.scheduledTime);
                String time = formatTimeFromIso(r.scheduledTime);

                String from = (r.startLocation != null) ? safe(r.startLocation.address) : "";
                String to = (r.endLocation != null) ? safe(r.endLocation.address) : "";

                int passengerCount = 0;
                if (r.passengers != null) passengerCount = r.passengers.size();
                else if (r.passengerEmails != null) passengerCount = r.passengerEmails.size();

                List<DriverDashboardAdapter.Requirement> reqs = new ArrayList<>();
                if (r.vehicleType != null) {
                    if ("VAN".equals(r.vehicleType)) reqs.add(DriverDashboardAdapter.Requirement.VAN);
                    else if ("STANDARD".equals(r.vehicleType)) reqs.add(DriverDashboardAdapter.Requirement.SEDAN);
                    else reqs.add(DriverDashboardAdapter.Requirement.SUV);
                }

                if (r.babyFriendly) reqs.add(DriverDashboardAdapter.Requirement.BABY);
                if (r.petsFriendly) reqs.add(DriverDashboardAdapter.Requirement.PETS);

                DriverDashboardAdapter.RideStatus st = DriverDashboardAdapter.RideStatus.SCHEDULED;
                if ("IN_PROGRESS".equals(r.status)) st = DriverDashboardAdapter.RideStatus.STARTED;
                else if ("ACCEPTED".equals(r.status)) st = DriverDashboardAdapter.RideStatus.ASSIGNED;

                mapped.add(new DriverDashboardAdapter.BookedRide(
                        date, time,
                        from, to,
                        passengerCount,
                        reqs,
                        st
                ));
            }
        }

        bookedAdapter.setItems(mapped);
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

    private void setBtnStopNoOp() {
        if (btnStartRide == null) return;
        btnStartRide.setText("Stop ride");
        btnStartRide.setEnabled(true);
        btnStartRide.setAlpha(1f);
        btnStartRide.setOnClickListener(v -> {
            // stop nista ne radi
        });
    }

}
