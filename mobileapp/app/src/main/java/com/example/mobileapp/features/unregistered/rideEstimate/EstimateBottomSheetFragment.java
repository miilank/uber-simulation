package com.example.mobileapp.features.unregistered.rideEstimate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.dto.PriceEstimateResponse;
import com.example.mobileapp.features.shared.api.dto.RideEstimateRequest;
import com.example.mobileapp.features.shared.input.LocationSearchInputFragment;
import com.example.mobileapp.features.shared.models.enums.VehicleType;
import com.example.mobileapp.features.shared.utils.Haversine;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstimateBottomSheetFragment extends BottomSheetDialogFragment {
    private LocationSearchInputFragment pickupSearch, destSearch;
    private Button getEstimateButton;
    double[] pickupLatLon,destLatLon;
    String pickupAddress,destinationAddress;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_estimate, container, false);

        pickupSearch = new LocationSearchInputFragment();
        destSearch = new LocationSearchInputFragment();
        getEstimateButton = view.findViewById(R.id.get_estimate_button);
        pickupSearch.setOnLocationSelectedListener(result -> {
            pickupLatLon = new double[]{result.lat, result.lon};
            pickupAddress = pickupSearch.getAddress();
        });

        destSearch.setOnLocationSelectedListener(result -> {
            destLatLon = new double[]{result.lat, result.lon};
            destinationAddress = destSearch.getAddress();
        });
        getEstimateButton.setOnClickListener(v -> {
            showEstimate();
        });
        getChildFragmentManager().beginTransaction()
                .replace(R.id.pickup_container, pickupSearch)
                .replace(R.id.dest_container, destSearch)
                .commit();

        return view;
    }
    private void showEstimate() {
        if (pickupLatLon == null || destLatLon == null) {
            Toast.makeText(getContext(), "Select both addresses", Toast.LENGTH_SHORT).show();
            return;
        }

        double distanceMeters = Haversine.calculateDistance(
                pickupLatLon[0], pickupLatLon[1],
                destLatLon[0], destLatLon[1]
        );

        RideEstimateRequest request = new RideEstimateRequest((int) distanceMeters, VehicleType.STANDARD);
        RidesApi rideApi = ApiClient.get().create(RidesApi.class);
        rideApi.estimateRide(request).enqueue(new Callback<PriceEstimateResponse>() {
            @Override
            public void onResponse(Call<PriceEstimateResponse> call, Response<PriceEstimateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResultsFragment results = ResultsFragment.newInstance(
                            pickupLatLon[0], pickupLatLon[1],
                            destLatLon[0], destLatLon[1],
                            response.body().getPriceDisplay(),
                            pickupAddress,destinationAddress,
                            distanceMeters

                    );
                    results.show(getParentFragmentManager(), "results");
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<PriceEstimateResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Estimate failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

