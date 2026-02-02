package com.example.mobileapp.features.driver.ridehistory;

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
import com.example.mobileapp.features.shared.models.Ride;
import com.example.mobileapp.features.shared.models.enums.RideStatus;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.dto.RideDto;
import com.example.mobileapp.features.shared.api.dto.RideHistoryResponseDto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideHistoryFragment extends Fragment {

    // ---------------------------- FILTERS UI ----------------------------
    private boolean filtersOpen = false;
    private View filtersPanel;
    private android.widget.ImageView ivArrow;

    // date inputs
    private android.widget.EditText etFrom, etTo;

    // animation
    private int filtersPanelHeight = 0;

    // ---------------------------- NETWORK / DATA ----------------------------
    private RidesApi ridesApi;

    // hardcoded driver until JWT/login is wired
    private final int driverId = 1;

    // list + adapter kept as fields so we can update them after API call
    private final List<Ride> rides = new ArrayList<>();
    private RideHistoryAdapter adapter;

    public RideHistoryFragment() {}

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_ride_history, container, false);

        // ---------------------------- FILTERS ----------------------------
        View filtersHeader = v.findViewById(R.id.card_filters);
        filtersPanel = v.findViewById(R.id.card_filters_panel);
        ivArrow = v.findViewById(R.id.iv_filters_arrow);

        // calculate panel height once (used for smooth expand/collapse)
        if (filtersPanel != null) {
            filtersPanel.post(() -> {
                filtersPanelHeight = filtersPanel.getHeight();
                filtersPanel.setVisibility(View.GONE);
            });
        }

        etFrom = v.findViewById(R.id.et_from);
        etTo = v.findViewById(R.id.et_to);

        View btnFromIcon = v.findViewById(R.id.btn_from_icon);
        View btnToIcon = v.findViewById(R.id.btn_to_icon);

        View.OnClickListener openFromPicker = vv -> showDatePicker(etFrom);
        View.OnClickListener openToPicker = vv -> showDatePicker(etTo);

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

                // reset filter -> load all rides again
                fetchRides(null, null);
            });
        }

        if (btnApply != null) {
            btnApply.setOnClickListener(vv -> {
                String fromIso = toIsoDate(etFrom != null ? etFrom.getText().toString() : null);
                String toIso = toIsoDate(etTo != null ? etTo.getText().toString() : null);

                // if user typed invalid format, notify and skip request
                if ((etFrom != null && !etFrom.getText().toString().trim().isEmpty() && fromIso == null) ||
                        (etTo != null && !etTo.getText().toString().trim().isEmpty() && toIso == null)) {
                    Toast.makeText(getContext(), "Invalid date format. Use dd/mm/yyyy.", Toast.LENGTH_SHORT).show();
                    return;
                }

                fetchRides(fromIso, toIso);
            });
        }

        if (btnLast7 != null) {
            btnLast7.setOnClickListener(vv -> {
                setLastDays(7);
                String fromIso = toIsoDate(etFrom != null ? etFrom.getText().toString() : null);
                String toIso = toIsoDate(etTo != null ? etTo.getText().toString() : null);
                fetchRides(fromIso, toIso);
            });
        }

        if (btnLast30 != null) {
            btnLast30.setOnClickListener(vv -> {
                setLastDays(30);
                String fromIso = toIsoDate(etFrom != null ? etFrom.getText().toString() : null);
                String toIso = toIsoDate(etTo != null ? etTo.getText().toString() : null);
                fetchRides(fromIso, toIso);
            });
        }

        // ---------------------------- RECYCLER VIEW ----------------------------
        RecyclerView rv = v.findViewById(R.id.rv_rides);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RideHistoryAdapter(rides, ride ->
                RideHistoryDetailsDialogFragment.newInstance(ride)
                        .show(getChildFragmentManager(), "ride_details")
        );
        rv.setAdapter(adapter);

        // ---------------------------- API INIT + INITIAL LOAD ----------------------------
        ridesApi = ApiClient.get().create(RidesApi.class);

        // initial load (no filters)
        fetchRides(null, null);

        // Filters card click
        if (filtersHeader != null) {
            filtersHeader.setOnClickListener(view -> toggleFilters());
        }

        // click outside input -> close keyboard
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

    private void fetchRides(@Nullable String startDateIso, @Nullable String endDateIso) {
        if (ridesApi == null) return;

        ridesApi.getRideHistory(driverId, startDateIso, endDateIso, null, null)
                .enqueue(new Callback<>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(@NonNull Call<RideHistoryResponseDto> call,
                                           @NonNull Response<RideHistoryResponseDto> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().rides == null) {
                            return;
                        }

                        rides.clear();
                        for (RideDto dto : response.body().rides) {
                            rides.add(mapDto(dto));
                        }
                        if (adapter != null) adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(@NonNull Call<RideHistoryResponseDto> call, @NonNull Throwable t) {
                        // keep UI silent by default
                        Toast.makeText(getContext(), "Failed to load rides.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Ride mapDto(@NonNull RideDto dto) {
        RideStatus st;
        try {
            st = RideStatus.valueOf(dto.status);
        } catch (Exception e) {
            st = RideStatus.SCHEDULED;
        }

        boolean panic = dto.panic != null && dto.panic;

        return new Ride(
                dto.date,
                dto.time,
                dto.from,
                dto.to,
                st,
                panic,
                dto.price,
                dto.cancelledBy
        );
    }

    /**
     * Converts dd/MM/yyyy -> yyyy-MM-dd (backend expects ISO like Angular date inputs).
     * Returns null if input is empty or invalid.
     */
    @Nullable
    private String toIsoDate(@Nullable String ddMMyyyy) {
        if (ddMMyyyy == null) return null;
        String s = ddMMyyyy.trim();
        if (s.isEmpty()) return null;

        try {
            SimpleDateFormat in = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            in.setLenient(false);

            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date d = in.parse(s);
            return d != null ? out.format(d) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void toggleFilters() {
        if (filtersPanel == null || ivArrow == null) return;

        // close keyboard when toggling filters
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
                R.style.MyDatePickerDialog,
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
                    R.color.app_accent
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
