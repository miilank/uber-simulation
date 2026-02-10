package com.example.mobileapp.features.admin.pricingManagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.PricingApi;
import com.example.mobileapp.features.shared.api.dto.PricingConfigDto;
import com.example.mobileapp.features.shared.api.dto.PricingUpdateRequestDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PricingManagementFragment extends Fragment {

    private RecyclerView recyclerViewPricing;
    private ProgressBar progressBar;
    private PricingAdapter adapter;
    private PricingApi pricingApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pricingApi = ApiClient.get().create(PricingApi.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pricing_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewPricing = view.findViewById(R.id.recyclerViewPricing);
        progressBar = view.findViewById(R.id.progressBar);

        setupRecyclerView();
        loadPricingData();
    }

    private void setupRecyclerView() {
        adapter = new PricingAdapter((config, request, position) -> {
            updatePricing(config, request, position);
        });

        recyclerViewPricing.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPricing.setHasFixedSize(true);
        recyclerViewPricing.setNestedScrollingEnabled(false);
        recyclerViewPricing.setAdapter(adapter);
    }

    private void loadPricingData() {
        showLoading(true);

        pricingApi.getAllPricing().enqueue(new Callback<List<PricingConfigDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<PricingConfigDto>> call,
                                   @NonNull Response<List<PricingConfigDto>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    adapter.setPricingList(response.body());
                } else {
                    showError("Failed to load pricing data");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PricingConfigDto>> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void updatePricing(PricingConfigDto config, PricingUpdateRequestDto request, int position) {
        showLoading(true);

        String vehicleType = config.getVehicleType().name();

        pricingApi.updatePricing(vehicleType, request).enqueue(new Callback<PricingConfigDto>() {
            @Override
            public void onResponse(@NonNull Call<PricingConfigDto> call,
                                   @NonNull Response<PricingConfigDto> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateItem(position, response.body());
                    showSuccess("Pricing updated successfully");
                } else {
                    showError("Failed to update pricing");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PricingConfigDto> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewPricing.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}