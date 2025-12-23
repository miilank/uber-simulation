package com.example.mobileapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.model.Ride;
import com.example.mobileapp.model.RideStatus;
import com.example.mobileapp.ui.adapters.RideHistoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class RideHistoryFragment extends Fragment {

    public RideHistoryFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_ride_history, container, false);

        // RecyclerView setup
        RecyclerView rv = v.findViewById(R.id.rv_rides);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Demo data (mock)
        List<Ride> rides = new ArrayList<>();
        rides.add(new Ride(
                "05.12.", "22:15 - 22:32",
                "Bulevar oslobođenja 76", "Naučno tehnološki park",
                RideStatus.COMPLETED, false, "€12.50", null
        ));
        rides.add(new Ride(
                "20.11.", "14:20 - 14:45",
                "Bulevar oslobođenja 55", "Naučno tehnološki park",
                RideStatus.CANCELLED, false, "€15.00", "User"
        ));
        rides.add(new Ride(
                "20.10.", "09:10 - 09:28",
                "Bulevar oslobođenja 76", "Naučno tehnološki park",
                RideStatus.COMPLETED, true, "€11.00", null
        ));
        rides.add(new Ride(
                "05.12.", "22:15 - 22:32",
                "Bulevar oslobođenja 76", "Naučno tehnološki park",
                RideStatus.COMPLETED, false, "€12.50", null
        ));
        rides.add(new Ride(
                "20.11.", "14:20 - 14:45",
                "Bulevar oslobođenja 55", "Naučno tehnološki park",
                RideStatus.CANCELLED, false, "€15.00", "User"
        ));
        rides.add(new Ride(
                "20.10.", "09:10 - 09:28",
                "Bulevar oslobođenja 76", "Naučno tehnološki park",
                RideStatus.COMPLETED, true, "€11.00", null
        ));
        rides.add(new Ride(
                "05.12.", "22:15 - 22:32",
                "Bulevar oslobođenja 76", "Naučno tehnološki park",
                RideStatus.COMPLETED, false, "€12.50", null
        ));
        rides.add(new Ride(
                "20.11.", "14:20 - 14:45",
                "Bulevar oslobođenja 55", "Naučno tehnološki park",
                RideStatus.CANCELLED, false, "€15.00", "User"
        ));
        rides.add(new Ride(
                "20.10.", "09:10 - 09:28",
                "Bulevar oslobođenja 76", "Naučno tehnološki park",
                RideStatus.COMPLETED, true, "€11.00", null
        ));

        // Adapter
        RideHistoryAdapter adapter = new RideHistoryAdapter(rides, ride ->
                Toast.makeText(getContext(), "Details: " + ride.getFrom(), Toast.LENGTH_SHORT).show()
        );
        rv.setAdapter(adapter);

        // Filters card click
        View filters = v.findViewById(R.id.card_filters);
        if (filters != null) {
            filters.setOnClickListener(view ->
                    Toast.makeText(getContext(), "Filters clicked", Toast.LENGTH_SHORT).show()
            );
        }

        return v;
    }
}
