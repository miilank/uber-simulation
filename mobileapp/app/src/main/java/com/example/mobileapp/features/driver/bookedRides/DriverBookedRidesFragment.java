package com.example.mobileapp.features.driver.bookedRides;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.driver.dashboard.DriverDashboardAdapter;
import com.example.mobileapp.features.driver.dashboard.DriverRidesService;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.dto.DriverRideDto;
import com.example.mobileapp.features.shared.api.dto.RideCancellationDto;
import com.example.mobileapp.features.shared.api.dto.RideDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverBookedRidesFragment extends Fragment {

    private RecyclerView rvBookedRides;
    private View emptyState;
    private DriverDashboardAdapter bookedAdapter;
    private DriverRidesService ridesService;
    private RidesApi ridesApi;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_driver_booked_rides, container, false);

        rvBookedRides = v.findViewById(R.id.rvBookedRides);
        emptyState = v.findViewById(R.id.emptyState);

        prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE);
        ridesApi = ApiClient.get().create(RidesApi.class);

        setupRecyclerView();

        ridesService = new DriverRidesService(requireContext());
        ridesService.bookedRides().observe(getViewLifecycleOwner(), this::renderBookedRides);
        ridesService.fetchRides();

        return v;
    }

    private void setupRecyclerView() {
        rvBookedRides.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookedAdapter = new DriverDashboardAdapter(new ArrayList<>());
        rvBookedRides.setAdapter(bookedAdapter);
    }

    private void renderBookedRides(@Nullable List<DriverRideDto> list) {
        List<DriverDashboardAdapter.BookedRide> mapped = new ArrayList<>();

        if (list != null) {
            for (DriverRideDto r : list) {
                if (r == null) continue;

                String date = formatDateFromIso(r.scheduledTime);
                String time = formatTimeFromIso(r.scheduledTime);
                String from = r.startLocation != null ? safe(r.startLocation.getAddress()) : "";
                String to = r.endLocation != null ? safe(r.endLocation.getAddress()) : "";

                int passengerCount = 0;
                if (r.passengers != null) {
                    passengerCount = r.passengers.size();
                } else if (r.passengerEmails != null) {
                    passengerCount = r.passengerEmails.size();
                }

                List<DriverDashboardAdapter.Requirement> reqs = new ArrayList<>();
                if (r.vehicleType != null) {
                    if ("VAN".equals(r.vehicleType)) {
                        reqs.add(DriverDashboardAdapter.Requirement.VAN);
                    } else if ("STANDARD".equals(r.vehicleType)) {
                        reqs.add(DriverDashboardAdapter.Requirement.SEDAN);
                    } else {
                        reqs.add(DriverDashboardAdapter.Requirement.LUXURY);
                    }
                }
                if (r.babyFriendly) reqs.add(DriverDashboardAdapter.Requirement.BABY);
                if (r.petsFriendly) reqs.add(DriverDashboardAdapter.Requirement.PETS);

                DriverDashboardAdapter.RideStatus st = DriverDashboardAdapter.RideStatus.SCHEDULED;
                if ("IN_PROGRESS".equals(r.status)) {
                    st = DriverDashboardAdapter.RideStatus.STARTED;
                } else if ("ACCEPTED".equals(r.status)) {
                    st = DriverDashboardAdapter.RideStatus.ASSIGNED;
                }

                mapped.add(new DriverDashboardAdapter.BookedRide(
                        r.id,
                        date,
                        time,
                        from,
                        to,
                        passengerCount,
                        reqs,
                        st
                ));
            }
        }

        bookedAdapter.setItems(mapped);

        if (mapped.isEmpty()) {
            rvBookedRides.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvBookedRides.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        bookedAdapter.setOnCancelClickListener(rideId -> {
            cancelRide(rideId);
        });
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

                    RideCancellationDto cancellationDto = new RideCancellationDto(userId, reason);

                    ridesApi.cancelRide("Bearer " + token, rideId, cancellationDto)
                            .enqueue(new Callback<RideDto>() {
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
                })
                .setNegativeButton("Keep Ride", null)
                .show();
    }

    private String safe(String s) {
        return s != null ? s : "";
    }

    private String formatDateFromIso(String iso) {
        if (iso == null) return "--.--.-";
        try {
            String s = iso;
            int dot = s.indexOf('.');
            if (dot != -1) s = s.substring(0, dot);

            String[] parts = s.split("T");
            if (parts.length == 0) return "--.--.-";

            String[] ymd = parts[0].split("-");
            if (ymd.length != 3) return "--.--.-";

            return ymd[2] + "." + ymd[1] + ".";
        } catch (Exception e) {
            return "--.--.-";
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
}
