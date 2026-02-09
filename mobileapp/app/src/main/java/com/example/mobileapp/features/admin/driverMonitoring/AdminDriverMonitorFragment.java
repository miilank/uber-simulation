package com.example.mobileapp.features.admin.driverMonitoring;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.AdminApi;
import com.example.mobileapp.features.shared.api.dto.DriverListItemDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDriverMonitorFragment extends Fragment {

    private EditText etSearch;
    private Button btnClear;
    private RecyclerView rvDrivers;
    private TextView tvResultCount;
    private ProgressBar pbLoading;
    private View layoutDashboard;
    private TextView tvMonitoringTitle;
    private Button btnCloseMonitor;

    // Filter animation
    private boolean filtersOpen = false;
    private View filtersPanel;
    private ImageView ivArrow;
    private int filtersPanelHeight = 0;

    private DriverListAdapter adapter;
    private AdminApi adminApi;
    private SharedPreferences prefs;

    private List<DriverListItemDto> allDrivers = new ArrayList<>();
    private List<DriverListItemDto> filteredDrivers = new ArrayList<>();
    private DriverListItemDto selectedDriver = null;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    public static AdminDriverMonitorFragment newInstance() {
        return new AdminDriverMonitorFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_driver_monitor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupFilterAnimation(view);
        setupRecyclerView();
        setupSearch();

        adminApi = ApiClient.get().create(AdminApi.class);
        prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE);

        loadDrivers();
        startAutoRefresh();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearchDriver);
        btnClear = view.findViewById(R.id.btnClearSearch);
        rvDrivers = view.findViewById(R.id.rvDriverList);
        tvResultCount = view.findViewById(R.id.tvResultCount);
        pbLoading = view.findViewById(R.id.pbLoadingDrivers);
        layoutDashboard = view.findViewById(R.id.layoutDashboard);
        tvMonitoringTitle = view.findViewById(R.id.tvMonitoringTitle);
        btnCloseMonitor = view.findViewById(R.id.btnCloseMonitor);

        btnClear.setOnClickListener(v -> clearSearch());
        btnCloseMonitor.setOnClickListener(v -> deselectDriver());
    }

    private void setupFilterAnimation(View view) {
        View filtersHeader = view.findViewById(R.id.card_filters);
        filtersPanel = view.findViewById(R.id.card_filters_panel);
        ivArrow = view.findViewById(R.id.iv_filters_arrow);

        // Calculate panel height once
        if (filtersPanel != null) {
            filtersPanel.post(() -> {
                filtersPanel.measure(
                        View.MeasureSpec.makeMeasureSpec(filtersPanel.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                filtersPanelHeight = filtersPanel.getMeasuredHeight();
                filtersPanel.setVisibility(View.GONE);
            });
        }

        // Filters card click
        if (filtersHeader != null) {
            filtersHeader.setOnClickListener(v -> toggleFilters());
        }
    }

    private void toggleFilters() {
        if (filtersPanel == null || ivArrow == null) return;

        // Close keyboard when toggling filters
        View current = requireActivity().getCurrentFocus();
        if (current instanceof EditText) {
            hideKeyboard(current);
            current.clearFocus();
        }

        if (!filtersOpen) {
            // OPEN
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
            // CLOSE
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

    @SuppressLint("ClickableViewAccessibility")
    private void setupRecyclerView() {
        rvDrivers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDrivers.setNestedScrollingEnabled(false);
        rvDrivers.setVerticalScrollBarEnabled(false);

        adapter = new DriverListAdapter(this::selectDriver);
        rvDrivers.setAdapter(adapter);

        // Koristi addOnItemTouchListener za presretanje touch event-a
        rvDrivers.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                // Blokiraj parent cim se dodirne RecyclerView
                rv.getParent().requestDisallowInterceptTouchEvent(true);

                // Dozvoli parent-u ponovo kada se pusti prst
                if (e.getAction() == android.view.MotionEvent.ACTION_UP ||
                        e.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    rv.getParent().requestDisallowInterceptTouchEvent(false);
                }

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDrivers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Close keyboard on touch outside
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
    }

    private void loadDrivers() {
        if (!isAdded()) return;

        String token = prefs.getString("jwt", null);
        if (token == null || token.isEmpty()) return;

        pbLoading.setVisibility(View.VISIBLE);

        adminApi.getAllDrivers("Bearer " + token).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<DriverListItemDto>> call,
                                   @NonNull Response<List<DriverListItemDto>> response) {
                if (!isAdded()) return;

                pbLoading.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) return;

                allDrivers = response.body();
                filterDrivers(etSearch.getText().toString());

                // Update selected driver if exists
                if (selectedDriver != null) {
                    for (DriverListItemDto d : allDrivers) {
                        if (d.id != null && d.id.equals(selectedDriver.id)) {
                            selectedDriver = d;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DriverListItemDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    private void filterDrivers(String query) {
        filteredDrivers.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredDrivers.addAll(allDrivers);
            btnClear.setVisibility(View.GONE);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (DriverListItemDto d : allDrivers) {
                String fullName = d.getFullName().toLowerCase();
                String email = d.email.toLowerCase();

                if (fullName.contains(lowerQuery) || email.contains(lowerQuery)) {
                    filteredDrivers.add(d);
                }
            }
            btnClear.setVisibility(View.VISIBLE);
        }

        adapter.setItems(filteredDrivers);
        tvResultCount.setText("Found " + filteredDrivers.size() + " driver(s)");
    }

    private void selectDriver(DriverListItemDto driver) {
        selectedDriver = driver;
        adapter.setSelectedDriverId(driver.id);

        tvMonitoringTitle.setText("Monitoring: " + driver.getFullName());
        layoutDashboard.setVisibility(View.VISIBLE);

        // Load dashboard fragment
        if (driver.hasActiveRide != null && driver.hasActiveRide) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.dashboardContainer,
                            AdminDriverDashboardFragment.newInstance(driver.email))
                    .commit();
        } else {
            // Show "No active ride" message
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.dashboardContainer,
                            NoActiveRideFragment.newInstance())
                    .commit();
        }
    }

    private void deselectDriver() {
        selectedDriver = null;
        adapter.setSelectedDriverId(null);
        layoutDashboard.setVisibility(View.GONE);
    }

    private void clearSearch() {
        etSearch.setText("");
        hideKeyboard(etSearch);
    }

    private void hideKeyboard(View view) {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadDrivers();
                refreshHandler.postDelayed(this, 10000);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 10000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
}