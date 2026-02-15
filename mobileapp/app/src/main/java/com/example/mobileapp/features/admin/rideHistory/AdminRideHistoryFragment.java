package com.example.mobileapp.features.admin.rideHistory;

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
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.dto.RideHistoryDto;
import com.example.mobileapp.features.shared.api.dto.RideHistoryResponseDto;
import com.example.mobileapp.features.shared.input.UserSearchInputView;
import com.example.mobileapp.features.shared.models.Ride;
import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.enums.RideStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminRideHistoryFragment extends Fragment {

    private android.content.SharedPreferences prefs;

    // ---------------------------- USER SEARCH ----------------------------
    private UserSearchInputView userSearch;

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

    // list + adapter kept as fields so we can update them after API call
    private final List<Ride> rides = new ArrayList<>();
    private AdminRideHistoryAdapter adapter;

    public AdminRideHistoryFragment() {}

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_ride_history, container, false);

        prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE);

        // ---------------------------- USER SEARCH ----------------------------
        userSearch = v.findViewById(R.id.user_search);
        if (userSearch != null) {
            userSearch.setOnUserClickCallback(() -> {
                fetchRides(null, null);
            });
        }
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
                fetchRides(null, null);
            });
        }

        if (btnApply != null) {
            btnApply.setOnClickListener(vv -> {
                String fromIso = toIsoDate(etFrom != null ? etFrom.getText().toString() : null);
                String toIso = toIsoDate(etTo != null ? etTo.getText().toString() : null);

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
        adapter = new AdminRideHistoryAdapter(rides, ride ->
                AdminRideHistoryDetailsDialogFragment.newInstance(ride)
                        .show(getChildFragmentManager(), "ride_details")
        );
        rv.setAdapter(adapter);

        // ---------------------------- API INIT ----------------------------
        ridesApi = ApiClient.get().create(RidesApi.class);

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

    // ---------------------------- FETCH RIDES ----------------------------
    private void fetchRides(@Nullable String startDateIso, @Nullable String endDateIso) {
        if (ridesApi == null) return;

        // Get selected user from UserSearchInputView
        User selectedUser = userSearch != null ? userSearch.getSelectedUser() : null;

        if (selectedUser == null || selectedUser.getId() == null) {
            Toast.makeText(getContext(), "Please select a user first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String auth = bearer();
        if (auth == null) {
            Toast.makeText(getContext(), "Not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer userId = selectedUser.getId();

        ridesApi.getPassengerRideHistory(auth, userId, startDateIso, endDateIso, 0, 50)
                .enqueue(new retrofit2.Callback<RideHistoryResponseDto>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<RideHistoryResponseDto> call,
                                           @NonNull retrofit2.Response<RideHistoryResponseDto> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                response.body().rides != null && !response.body().rides.isEmpty()) {
                            rides.clear();
                            for (RideHistoryDto dto : response.body().rides) {
                                rides.add(mapDto(dto));
                            }
                            if (adapter != null) adapter.notifyDataSetChanged();
                        } else {
                            fetchDriverRides(auth, userId, startDateIso, endDateIso);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<RideHistoryResponseDto> call,
                                          @NonNull Throwable t) {
                        fetchDriverRides(auth, userId, startDateIso, endDateIso);
                    }
                });
    }
    private void fetchDriverRides(String auth, Integer userId,
                                  @Nullable String startDateIso, @Nullable String endDateIso) {
        ridesApi.getRideHistory(auth, userId, startDateIso, endDateIso, 0, 50)
                .enqueue(new retrofit2.Callback<RideHistoryResponseDto>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<RideHistoryResponseDto> call,
                                           @NonNull retrofit2.Response<RideHistoryResponseDto> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().rides != null) {
                            rides.clear();
                            for (RideHistoryDto dto : response.body().rides) {
                                rides.add(mapDto(dto));
                            }
                            if (adapter != null) adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "No rides found for this user.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<RideHistoryResponseDto> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load rides.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private Ride mapDto(@NonNull RideHistoryDto dto) {
        RideStatus st;
        try {
            st = RideStatus.valueOf(dto.status);
        } catch (Exception e) {
            st = RideStatus.SCHEDULED;
        }

        boolean panic = dto.panic != null && dto.panic;
        return new Ride(
                dto.id,
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
                    String dd = String.format(Locale.getDefault(), "%02d", dayOfMonth);
                    String mm = String.format(Locale.getDefault(), "%02d", month + 1);
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
        String dd = String.format(Locale.getDefault(), "%02d", c.get(java.util.Calendar.DAY_OF_MONTH));
        String mm = String.format(Locale.getDefault(), "%02d", c.get(java.util.Calendar.MONTH) + 1);
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

    @Nullable
    private String bearer() {
        String token = prefs != null ? prefs.getString("jwt", null) : null;
        if (token == null || token.trim().isEmpty()) return null;
        return "Bearer " + token;
    }
}
