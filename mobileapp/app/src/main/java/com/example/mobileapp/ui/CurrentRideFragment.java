package com.example.mobileapp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobileapp.model.PassengerItem;
import com.example.mobileapp.ui.adapters.PassengerCurrentRideAdapter;

import com.example.mobileapp.R;

import java.util.ArrayList;
import java.util.List;

public class CurrentRideFragment extends Fragment {

    private enum RideStatus { ASSIGNED, STARTED, FINISHED, CANCELLED }

    private RideStatus currentRideStatus = RideStatus.STARTED;

    private final String fromAddress = "Bulevar oslobođenja 46";
    private final String toAddress = "Ulica Narodnih heroja 14";
    private final String vehicleText = "Vehicle: Skoda Octavia • NS-123-AB";

    private int etaMinutes = 7;

    private android.widget.TextView tvRideStatus;
    private android.widget.TextView tvEta;
    private android.widget.TextView tvRoute;
    private android.widget.TextView tvVehicle;
    private android.widget.TextView tvCharCount;
    private android.widget.TextView tvGuard;
    private android.widget.TextView btnSubmit;

    private android.widget.EditText etReportNote;

    private boolean submitting = false;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public CurrentRideFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupPassengers(view);
        setupReport();
        bindMockData();

        applyStatusStyle();
        refreshEta();
        refreshActiveGuard();
        setupMapChild();
    }

    private void bindViews(View view) {
        tvRideStatus = view.findViewById(R.id.tvRideStatus);
        tvEta = view.findViewById(R.id.tvEta);
        tvRoute = view.findViewById(R.id.tvRoute);
        tvVehicle = view.findViewById(R.id.tvVehicle);
        tvCharCount = view.findViewById(R.id.tvCharCount);
        tvGuard = view.findViewById(R.id.tvGuard);
        btnSubmit = view.findViewById(R.id.btnSubmitReport);
        etReportNote = view.findViewById(R.id.etReportNote);
    }

    @SuppressLint("SetTextI18n")
    private void bindMockData() {
        tvRoute.setText(fromAddress + " → " + toAddress);
        tvVehicle.setText(vehicleText);
    }

    private void setupPassengers(View view) {
        RecyclerView rvPassengers = view.findViewById(R.id.rvPassengers);
        rvPassengers.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<PassengerItem> list = new ArrayList<>();
        list.add(new PassengerItem(1, "Milan Kacarevic", "You"));
        list.add(new PassengerItem(2, "Mirko Mirkovic", "Passenger"));
        list.add(new PassengerItem(3, "Ana Jovanovic", "Passenger"));
        list.add(new PassengerItem(4, "Ana Markovic", "Passenger"));

        rvPassengers.setAdapter(new PassengerCurrentRideAdapter(list));
    }

    private void setupReport() {
        updateCharCount();
        updateSubmitState();

        etReportNote.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharCount();
                updateSubmitState();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private boolean isRideActive() {
        return currentRideStatus == RideStatus.STARTED || currentRideStatus == RideStatus.ASSIGNED;
    }

    @SuppressLint("SetTextI18n")
    private void refreshEta() {
        if (!isRideActive()) {
            tvEta.setText("ETA: --");
        } else if (etaMinutes <= 1) {
            tvEta.setText("ETA: < 1 min");
        } else {
            tvEta.setText("ETA: " + etaMinutes + " min");
        }
    }

    private void refreshActiveGuard() {
        if (!isRideActive()) {
            tvGuard.setVisibility(View.VISIBLE);
            etReportNote.setEnabled(false);
            btnSubmit.setEnabled(false);
            btnSubmit.setAlpha(0.5f);
        } else {
            tvGuard.setVisibility(View.GONE);
            etReportNote.setEnabled(true);
            updateSubmitState();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateCharCount() {
        String txt = etReportNote.getText() == null ? "" : etReportNote.getText().toString();
        tvCharCount.setText(txt.length() + "/300");
    }

    private void updateSubmitState() {
        if (!isRideActive() || submitting) {
            btnSubmit.setEnabled(false);
            btnSubmit.setAlpha(0.5f);
            return;
        }

        String note = etReportNote.getText() == null ? "" : etReportNote.getText().toString().trim();
        boolean enabled = !note.isEmpty();
        btnSubmit.setEnabled(enabled);
        btnSubmit.setAlpha(enabled ? 1f : 0.5f);
    }

    @SuppressLint("SetTextI18n")
    private void submitReport() {
        if (!isRideActive()) return;

        String note = etReportNote.getText() == null ? "" : etReportNote.getText().toString().trim();
        if (note.isEmpty()) return;

        submitting = true;
        btnSubmit.setText("Sending...");
        updateSubmitState();

        handler.postDelayed(() -> {
            submitting = false;
            if (etReportNote.getText() != null) etReportNote.getText().clear();
            btnSubmit.setText("Submit");
            updateCharCount();
            updateSubmitState();
        }, 600);
    }

    @SuppressLint("SetTextI18n")
    private void applyStatusStyle() {
        switch (currentRideStatus) {
            case ASSIGNED:
                tvRideStatus.setText("Assigned");
                tvRideStatus.setBackgroundResource(R.drawable.bg_assigned);
                tvRideStatus.setTextColor(0xFF1D4ED8);
                break;
            case STARTED:
                tvRideStatus.setText("Started");
                tvRideStatus.setBackgroundResource(R.drawable.bg_started);
                tvRideStatus.setTextColor(0xFF065F46);
                break;
            case FINISHED:
                tvRideStatus.setText("Completed");
                tvRideStatus.setBackgroundResource(R.drawable.bg_completed);
                tvRideStatus.setTextColor(0xFF374151);
                break;
            case CANCELLED:
                tvRideStatus.setText("Cancelled");
                tvRideStatus.setBackgroundResource(R.drawable.bg_cancelled);
                tvRideStatus.setTextColor(0xFF7F1D1D);
                break;
        }
    }

    private void setupMapChild() {
        if (getChildFragmentManager().findFragmentById(R.id.mapContainer) == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, new MapFragment())
                    .commit();
        }
    }
}
