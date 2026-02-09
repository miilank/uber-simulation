package com.example.mobileapp.features.unregistered.rideEstimate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.example.mobileapp.R;
import com.example.mobileapp.core.auth.LoginFragment;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.dto.PriceEstimateResponse;
import com.example.mobileapp.features.shared.api.dto.RideEstimateRequest;
import com.example.mobileapp.features.shared.models.enums.VehicleType;
import com.example.mobileapp.features.shared.utils.Haversine;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultsFragment extends DialogFragment {
    private static final String ARG_PLAT = "plat", ARG_PLON = "plon";
    private static final String ARG_DLAT = "dlat", ARG_DLON = "dlon";
    private static String ARG_PRICE = "price";
    private static String ARG_DISTANCE = "distance";
    private static final String ARG_PICKUP = "pickup", ARG_DESTINATION = "destination";

    private TextView priceView;


    private VehicleType selectedServiceType = VehicleType.STANDARD;

    public static ResultsFragment newInstance(double pLat, double pLon, double dLat, double dLon, String price, String pickup, String destination, double distance) {
        ResultsFragment f = new ResultsFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_PLAT, pLat);
        args.putDouble(ARG_PLON, pLon);
        args.putDouble(ARG_DLAT, dLat);
        args.putDouble(ARG_DLON, dLon);
        args.putString(ARG_PRICE, price);
        args.putDouble(ARG_DISTANCE, distance);
        args.putString(ARG_PICKUP, pickup);
        args.putString(ARG_DESTINATION, destination);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_results_dialog, null);

        Bundle args = getArguments();
        if (args != null) {
            priceView = view.findViewById(R.id.tv_price_range);
            priceView.setText(args.getString(ARG_PRICE, "€0.00"));

            TextView distanceView = view.findViewById(R.id.tv_distance);
            double metersDistance = (Haversine.calculateDistance(args.getDouble(ARG_PLAT),args.getDouble(ARG_PLON),args.getDouble(ARG_DLAT),args.getDouble(ARG_DLON)));
            String kilometersString = String.format("%.2f",metersDistance / 1000) + " km";
            distanceView.setText(kilometersString);

            TextView pickupAddress = view.findViewById(R.id.tv_pickup_label);
            pickupAddress.setText(args.getString(ARG_PICKUP));

            TextView destinationAddress = view.findViewById(R.id.tv_dropoff_label);
            destinationAddress.setText(args.getString(ARG_DESTINATION));

        }

        setupServiceTypeButtons(view);
        setupActionButtons(view);

        builder.setView(view);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        return dialog;
    }

    private void setupServiceTypeButtons(View view) {
        CardView standardBtn = view.findViewById(R.id.btn_standard);
        CardView deluxeBtn = view.findViewById(R.id.btn_deluxe);
        CardView extraLargeBtn = view.findViewById(R.id.btn_extra_large);

        standardBtn.setOnClickListener(v -> selectServiceType(VehicleType.STANDARD, standardBtn, deluxeBtn, extraLargeBtn));
        deluxeBtn.setOnClickListener(v -> selectServiceType(VehicleType.LUXURY, standardBtn, deluxeBtn, extraLargeBtn));
        extraLargeBtn.setOnClickListener(v -> selectServiceType(VehicleType.VAN, standardBtn, deluxeBtn, extraLargeBtn));
    }

    private void selectServiceType(VehicleType type, CardView standardBtn, CardView deluxeBtn, CardView extraLargeBtn) {
        selectedServiceType = type;

        resetButtonStyle(standardBtn);
        resetButtonStyle(deluxeBtn);
        resetButtonStyle(extraLargeBtn);

        CardView selectedButton;
        switch (selectedServiceType) {
            case LUXURY:
                selectedButton = deluxeBtn;
                break;
            case VAN:
                selectedButton = extraLargeBtn;
                break;
            default:
                selectedButton = standardBtn;
                break;
        }

        highlightButton(selectedButton);

        updatePrice(selectedServiceType);
    }

    private void resetButtonStyle(CardView button) {
        button.setCardBackgroundColor(Color.parseColor("#F3F3F5"));
        TextView textView = (TextView) button.getChildAt(0);
        textView.setTextColor(Color.parseColor("#333333"));
        textView.setTypeface(null,Typeface.NORMAL);
    }

    private void highlightButton(CardView button) {
        button.setCardBackgroundColor(Color.parseColor("#2D7A5F"));
        TextView textView = (TextView) button.getChildAt(0);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(null,Typeface.BOLD);

    }

    private void setupActionButtons(View view) {
        MaterialCardView backToMapBtn = view.findViewById(R.id.btn_back_to_map);
        CardView bookRideBtn = view.findViewById(R.id.btn_book_ride);

        backToMapBtn.setOnClickListener(v -> {
            dismiss();
        });

        bookRideBtn.setOnClickListener(v -> {
            handleBookRide();
        });
    }

    private void handleBookRide() {
        LoginFragment loginFragment = new LoginFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, loginFragment)  // YOUR container ID
                .addToBackStack(null)
                .commit();

        dismiss();
    }

    private void updatePrice(VehicleType type){
        Bundle args = getArguments();
        if (args != null) {

            RideEstimateRequest request = new RideEstimateRequest((int) args.getDouble(ARG_DISTANCE), type);
            RidesApi rideApi = ApiClient.get().create(RidesApi.class);
            rideApi.estimateRide(request).enqueue(new Callback<PriceEstimateResponse>() {
                @Override
                public void onResponse(Call<PriceEstimateResponse> call, Response<PriceEstimateResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        priceView.setText(response.body().getPriceDisplay());
                    }
                }

                @Override
                public void onFailure(Call<PriceEstimateResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Estimate failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
