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
import com.example.mobileapp.features.shared.map.MapFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DriverDashboardFragment extends Fragment {

    private RecyclerView rvPassengers;
    private RecyclerView rvBookedRides;

    private ProgressBar pbWork;
    private TextView tvWorkActive;
    private TextView tvWorkLimit;

    private final int workMinutes = 265;
    private final int workLimitMinutes = 480;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_driver_dashboard, container, false);

        pbWork = v.findViewById(R.id.pbWork);
        tvWorkActive = v.findViewById(R.id.tvWorkActive);
        tvWorkLimit = v.findViewById(R.id.tvWorkLimit);

        rvPassengers = v.findViewById(R.id.rvPassengers);
        rvBookedRides = v.findViewById(R.id.rvBookedRides);

        setupWorkingHours();
        setupPassengers();
        setupBookedRides();
        setupMapChild();

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
    private void setupPassengers() {
        rvPassengers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPassengers.setNestedScrollingEnabled(true);

        List<Passenger> passengers = Arrays.asList(
                new Passenger("Mirko Mirkovic", "+381 63 111 2222"),
                new Passenger("Jovan Markovic", "+381 63 222 3333"),
                new Passenger("Milan Kacarevic", "+381 62 333 4444"),
                new Passenger("Luka Petrovic", "+381 60 555 6666"),
                new Passenger("Ana Jovanovic", "+381 69 777 8888"),
                new Passenger("Nikola Ilic", "+381 64 123 4567"),
                new Passenger("Marija Nikolic", "+381 65 222 1111"),
                new Passenger("Stefan Markovic", "+381 61 444 7777")
        );

        rvPassengers.setAdapter(new PassengerAdapter(passengers));

        rvPassengers.setOnTouchListener((view, event) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            int a = event.getActionMasked();
            if (a == android.view.MotionEvent.ACTION_UP || a == android.view.MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                view.performClick();
            }
            return false;
        });
    }

    private void setupBookedRides() {
        rvBookedRides.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBookedRides.setNestedScrollingEnabled(false);

        List<DriverDashboardAdapter.BookedRide> rides = new ArrayList<>();
        rides.add(new DriverDashboardAdapter.BookedRide(
                "18.08.", "22:30",
                "Bulevar M. Pupina 10", "Trg slobode 1",
                2,
                Arrays.asList(DriverDashboardAdapter.Requirement.SEDAN, DriverDashboardAdapter.Requirement.BABY),
                DriverDashboardAdapter.RideStatus.SCHEDULED
        ));
        rides.add(new DriverDashboardAdapter.BookedRide(
                "18.08.", "22:40",
                "Laze Telečkog 5", "Bul. cara Lazara 56",
                1,
                List.of(DriverDashboardAdapter.Requirement.SUV),
                DriverDashboardAdapter.RideStatus.SCHEDULED
        ));
        rides.add(new DriverDashboardAdapter.BookedRide(
                "18.08.", "23:10",
                "Bulevar oslobođenja 1", "Dunavski park",
                3,
                Arrays.asList(
                        DriverDashboardAdapter.Requirement.VAN,
                        DriverDashboardAdapter.Requirement.PETS,
                        DriverDashboardAdapter.Requirement.BABY
                ),
                DriverDashboardAdapter.RideStatus.SCHEDULED
        ));

        rvBookedRides.setAdapter(new DriverDashboardAdapter(rides));
    }

    private void setupMapChild() {
        if (getChildFragmentManager().findFragmentById(R.id.mapContainer) == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, new MapFragment())
                    .commit();
        }
    }

    private String formatMinutes(int total) {
        int h = total / 60;
        int m = total % 60;
        return String.format(Locale.getDefault(), "%dh %02dmin", h, m);
    }

    private static final class Passenger {
        final String name;
        final String phone;

        Passenger(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }
    }

    private static final class PassengerAdapter extends RecyclerView.Adapter<PassengerAdapter.PassengerVH> {

        private final List<Passenger> items;

        PassengerAdapter(List<Passenger> items) {
            this.items = items;
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
            h.tvPhone.setText(p.phone);
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
}
