package com.example.mobileapp.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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

    // needed for Filetrs
    private boolean filtersOpen = false;
    private View filtersPanel;
    private android.widget.ImageView ivArrow;

    // needed for datepicker
    private android.widget.EditText etFrom, etTo;

    // needed for animation
    private int filtersPanelHeight = 0;

    public RideHistoryFragment() {}

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Load Fragment XML Design
        View v = inflater.inflate(R.layout.fragment_ride_history, container, false);

        // ---------------------------- FILTERS ----------------------------
        // Needed for opening and closing Filters
        View filtersHeader = v.findViewById(R.id.card_filters);
        filtersPanel = v.findViewById(R.id.card_filters_panel);
        ivArrow = v.findViewById(R.id.iv_filters_arrow);

        // for animation, we calculate height of the panel when it is first time drawn
        filtersPanel.post(() -> {
            filtersPanelHeight = filtersPanel.getHeight();
            filtersPanel.setVisibility(View.GONE);
        });

        // Needed for datepicker
        etFrom = v.findViewById(R.id.et_from);
        etTo = v.findViewById(R.id.et_to);

        // Buttons are initialized as local variables
        View btnFromIcon = v.findViewById(R.id.btn_from_icon);
        View btnToIcon = v.findViewById(R.id.btn_to_icon);

        View.OnClickListener openFromPicker = vv -> showDatePicker(etFrom);
        View.OnClickListener openToPicker = vv -> showDatePicker(etTo);

        // Click on input fields or icons
        if (etFrom != null) etFrom.setOnClickListener(openFromPicker);
        if (btnFromIcon != null) btnFromIcon.setOnClickListener(openFromPicker);

        if (etTo != null) etTo.setOnClickListener(openToPicker);
        if (btnToIcon != null) btnToIcon.setOnClickListener(openToPicker);

        android.widget.Button btnApply = v.findViewById(R.id.btn_apply);
        android.widget.Button btnReset = v.findViewById(R.id.btn_reset);
        android.widget.Button btnLast7 = v.findViewById(R.id.btn_last_7);
        android.widget.Button btnLast30 = v.findViewById(R.id.btn_last_30);

        if (btnReset != null) {
            btnReset.setOnClickListener(vv -> {
                if (etFrom != null) etFrom.setText("");
                if (etTo != null) etTo.setText("");
            });
        }

        if (btnApply != null) {
            btnApply.setOnClickListener(vv -> Toast.makeText(getContext(),
                    "Apply: " + (etFrom != null ? etFrom.getText().toString() : "") +
                            " → " + (etTo != null ? etTo.getText().toString() : ""),
                    Toast.LENGTH_SHORT).show());
        }

        if (btnLast7 != null) btnLast7.setOnClickListener(vv -> setLastDays(7));
        if (btnLast30 != null) btnLast30.setOnClickListener(vv -> setLastDays(30));

        // ---------------------------- RECYCLER VIEW ----------------------------
        RecyclerView rv = v.findViewById(R.id.rv_rides);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Demo data (later from database)
        List<Ride> rides = new ArrayList<>();
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
                "20.11.", "14:20 - 14:45",
                "Bulevar oslobođenja 55", "Naučno tehnološki park",
                RideStatus.CANCELLED, false, "€15.00", "User"
        ));

        // Adapter
        RideHistoryAdapter adapter = new RideHistoryAdapter(rides, ride ->
                Toast.makeText(getContext(), "Details: " + ride.getFrom(), Toast.LENGTH_SHORT).show()
        );
        rv.setAdapter(adapter);

        // Filters card click
        if (filtersHeader != null) {
            filtersHeader.setOnClickListener(view -> toggleFilters());
        }

        // if we click everywhere except input date fields, keyboard closes
        v.setOnTouchListener((view, event) -> {
            View current = requireActivity().getCurrentFocus();
            if (current instanceof android.widget.EditText) {
                hideKeyboard(current);
                current.clearFocus();
            }
            return false;
        });

        return v;
    }

    private void toggleFilters() {
        if (filtersPanel == null || ivArrow == null) return;

        // if we open or close filters keyboard closes
        View current = requireActivity().getCurrentFocus();
        if (current instanceof android.widget.EditText) {
            hideKeyboard(current);
            current.clearFocus();
        }

        if (!filtersOpen) {
            filtersOpen = true;

            filtersPanel.setVisibility(View.VISIBLE);
            filtersPanel.getLayoutParams().height = 0;
            filtersPanel.requestLayout();

            int targetHeight = filtersPanelHeight > 0 ? filtersPanelHeight : ViewGroup.LayoutParams.WRAP_CONTENT;

            ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
            animator.setDuration(200);
            animator.addUpdateListener(animation -> {
                filtersPanel.getLayoutParams().height = (int) animation.getAnimatedValue();
                filtersPanel.requestLayout();
            });
            animator.start();

            ivArrow.setImageResource(R.drawable.ic_down);
        } else {
            filtersOpen = false;

            int startHeight = filtersPanel.getHeight();
            ValueAnimator animator = ValueAnimator.ofInt(startHeight, 0);
            animator.setDuration(200);
            animator.addUpdateListener(animation -> {
                int value = (int) animation.getAnimatedValue();
                filtersPanel.getLayoutParams().height = value;
                filtersPanel.requestLayout();
                if (value == 0) {
                    filtersPanel.setVisibility(View.GONE);
                }
            });
            animator.start();

            ivArrow.setImageResource(R.drawable.ic_right);
        }
    }

    private void showDatePicker(android.widget.EditText target) {
        if (target == null) return;
        hideKeyboard(target);
        java.util.Calendar c = java.util.Calendar.getInstance();

        @SuppressLint("SetTextI18n")
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(
                requireContext(),
                R.style.MyDatePickerDialog,   // style inserted
                (view, year, month, dayOfMonth) -> {
                    String dd = String.format(java.util.Locale.getDefault(), "%02d", dayOfMonth);
                    String mm = String.format(java.util.Locale.getDefault(), "%02d", month + 1);
                    String yyyy = String.valueOf(year);
                    target.setText(dd + "/" + mm + "/" + yyyy);
                },
                c.get(java.util.Calendar.YEAR),
                c.get(java.util.Calendar.MONTH),
                c.get(java.util.Calendar.DAY_OF_MONTH)
        );

        dialog.setOnShowListener(d -> {
            int accent = androidx.core.content.ContextCompat.getColor(
                    requireContext(),
                    R.color.app_accent   // the sam color we use in MyDatePickerDialog
            );
            android.widget.Button positive = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
            android.widget.Button negative = dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE);

            if (positive != null) positive.setTextColor(accent);
            if (negative != null) negative.setTextColor(accent);
        });

        dialog.show();
    }

    private void setLastDays(int days) {
        if (etFrom == null || etTo == null) return;

        java.util.Calendar end = java.util.Calendar.getInstance();
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.add(java.util.Calendar.DAY_OF_YEAR, -days);

        etFrom.setText(formatDate(start));
        etTo.setText(formatDate(end));
    }

    private String formatDate(java.util.Calendar c) {
        String dd = String.format(java.util.Locale.getDefault(), "%02d", c.get(java.util.Calendar.DAY_OF_MONTH));
        String mm = String.format(java.util.Locale.getDefault(), "%02d", c.get(java.util.Calendar.MONTH) + 1);
        String yyyy = String.valueOf(c.get(java.util.Calendar.YEAR));
        return dd + "/" + mm + "/" + yyyy;
    }

    private void hideKeyboard(View view) {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
