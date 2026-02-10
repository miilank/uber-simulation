package com.example.mobileapp.features.admin.pricingManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.PricingConfigDto;
import com.example.mobileapp.features.shared.api.dto.PricingUpdateRequestDto;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PricingAdapter extends RecyclerView.Adapter<PricingAdapter.PricingViewHolder> {

    private List<PricingConfigDto> pricingList = new ArrayList<>();
    private Integer editingPosition = null;
    private OnPricingUpdateListener updateListener;

    public interface OnPricingUpdateListener {
        void onUpdate(PricingConfigDto config, PricingUpdateRequestDto request, int position);
    }

    public PricingAdapter(OnPricingUpdateListener listener) {
        this.updateListener = listener;
    }

    public void setPricingList(List<PricingConfigDto> list) {
        this.pricingList = list;
        notifyDataSetChanged();
    }

    public void updateItem(int position, PricingConfigDto config) {
        if (position >= 0 && position < pricingList.size()) {
            pricingList.set(position, config);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public PricingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pricing_card, parent, false);
        return new PricingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PricingViewHolder holder, int position) {
        PricingConfigDto config = pricingList.get(position);
        holder.bind(config, position);
    }

    @Override
    public int getItemCount() {
        return pricingList.size();
    }

    class PricingViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleIcon, tvVehicleType, tvVehicleDescription;
        TextView tvBasePrice, tvPricePerKm, tvExamplePrice;
        TextView tvLastUpdated, tvUpdatedBy;
        Button btnEdit;
        LinearLayout layoutDisplayMode, layoutEditMode;
        TextInputLayout tilBasePrice, tilPricePerKm;
        TextInputEditText etBasePrice, etPricePerKm;
        Button btnSave, btnCancel;

        public PricingViewHolder(@NonNull View itemView) {
            super(itemView);

            // Display mode
            tvVehicleIcon = itemView.findViewById(R.id.tvVehicleIcon);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvVehicleDescription = itemView.findViewById(R.id.tvVehicleDescription);
            tvBasePrice = itemView.findViewById(R.id.tvBasePrice);
            tvPricePerKm = itemView.findViewById(R.id.tvPricePerKm);
            tvExamplePrice = itemView.findViewById(R.id.tvExamplePrice);
            tvLastUpdated = itemView.findViewById(R.id.tvLastUpdated);
            tvUpdatedBy = itemView.findViewById(R.id.tvUpdatedBy);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            layoutDisplayMode = itemView.findViewById(R.id.layoutDisplayMode);

            // Edit mode
            layoutEditMode = itemView.findViewById(R.id.layoutEditMode);
            tilBasePrice = itemView.findViewById(R.id.tilBasePrice);
            tilPricePerKm = itemView.findViewById(R.id.tilPricePerKm);
            etBasePrice = itemView.findViewById(R.id.etBasePrice);
            etPricePerKm = itemView.findViewById(R.id.etPricePerKm);
            btnSave = itemView.findViewById(R.id.btnSave);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        public void bind(PricingConfigDto config, int position) {
            // Set vehicle info
            tvVehicleIcon.setText(config.getVehicleType().getIcon());
            tvVehicleType.setText(config.getVehicleType().getLabel());
            tvVehicleDescription.setText(config.getVehicleType().getDescription());

            // Set prices
            tvBasePrice.setText(String.format(Locale.getDefault(), "€%.2f", config.getBasePrice()));
            tvPricePerKm.setText(String.format(Locale.getDefault(), "€%.2f", config.getPricePerKm()));

            // Calculate and display example
            double examplePrice = config.calculateExamplePrice(10.0);
            tvExamplePrice.setText(String.format(Locale.getDefault(), "€%.2f", examplePrice));

            // Format date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            tvLastUpdated.setText(config.getLastUpdated().format(formatter));
            tvUpdatedBy.setText("by " + config.getUpdatedBy());

            // Handle edit mode toggle
            boolean isEditing = editingPosition != null && editingPosition == position;
            layoutDisplayMode.setVisibility(isEditing ? View.GONE : View.VISIBLE);
            layoutEditMode.setVisibility(isEditing ? View.VISIBLE : View.GONE);

            if (isEditing) {
                etBasePrice.setText(String.format(Locale.getDefault(), "%.2f", config.getBasePrice()));
                etPricePerKm.setText(String.format(Locale.getDefault(), "%.2f", config.getPricePerKm()));
            }

            // Edit button
            btnEdit.setOnClickListener(v -> {
                editingPosition = position;
                notifyDataSetChanged();
            });

            // Cancel button
            btnCancel.setOnClickListener(v -> {
                editingPosition = null;
                tilBasePrice.setError(null);
                tilPricePerKm.setError(null);
                notifyDataSetChanged();
            });

            // Save button
            btnSave.setOnClickListener(v -> {
                String basePriceStr = etBasePrice.getText().toString().trim();
                String pricePerKmStr = etPricePerKm.getText().toString().trim();

                boolean isValid = true;

                if (basePriceStr.isEmpty()) {
                    tilBasePrice.setError("Base price is required");
                    isValid = false;
                } else {
                    try {
                        double basePrice = Double.parseDouble(basePriceStr);
                        if (basePrice < 0) {
                            tilBasePrice.setError("Base price must be positive");
                            isValid = false;
                        } else {
                            tilBasePrice.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        tilBasePrice.setError("Invalid number");
                        isValid = false;
                    }
                }

                if (pricePerKmStr.isEmpty()) {
                    tilPricePerKm.setError("Price per km is required");
                    isValid = false;
                } else {
                    try {
                        double pricePerKm = Double.parseDouble(pricePerKmStr);
                        if (pricePerKm < 0) {
                            tilPricePerKm.setError("Price per km must be positive");
                            isValid = false;
                        } else {
                            tilPricePerKm.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        tilPricePerKm.setError("Invalid number");
                        isValid = false;
                    }
                }

                if (isValid) {
                    PricingUpdateRequestDto request = new PricingUpdateRequestDto(
                            Double.parseDouble(basePriceStr),
                            Double.parseDouble(pricePerKmStr)
                    );

                    editingPosition = null;
                    if (updateListener != null) {
                        updateListener.onUpdate(config, request, position);
                    }
                }
            });
        }
    }
}