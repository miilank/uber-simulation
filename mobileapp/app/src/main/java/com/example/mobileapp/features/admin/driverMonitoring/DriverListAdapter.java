package com.example.mobileapp.features.admin.driverMonitoring;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.DriverListItemDto;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class DriverListAdapter extends RecyclerView.Adapter<DriverListAdapter.DriverVH> {

    public interface OnDriverClickListener {
        void onDriverClick(DriverListItemDto driver);
    }

    private final List<DriverListItemDto> items = new ArrayList<>();
    private final OnDriverClickListener listener;
    private Integer selectedDriverId = null;

    public DriverListAdapter(OnDriverClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<DriverListItemDto> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setSelectedDriverId(Integer driverId) {
        this.selectedDriverId = driverId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DriverVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driver_list, parent, false);
        return new DriverVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverVH h, int position) {
        DriverListItemDto d = items.get(position);

        h.tvName.setText(d.getFullName());
        h.tvEmail.setText(d.email);

        // Status badge
        if (d.hasActiveRide != null && d.hasActiveRide) {
            h.tvStatus.setText("Active Ride");
            h.tvStatus.setBackgroundResource(R.drawable.bg_status_active_ride);
        } else {
            h.tvStatus.setText("No Active Ride");
            h.tvStatus.setBackgroundResource(R.drawable.bg_status_no_ride);
        }

        // Selection highlight
        boolean isSelected = selectedDriverId != null && selectedDriverId.equals(d.id);
        if (isSelected) {
            h.card.setCardBackgroundColor(h.itemView.getContext().getColor(R.color.blue_100));
            h.card.setStrokeColor(h.itemView.getContext().getColor(R.color.blue_500));
            h.card.setStrokeWidth(4);
        } else {
            h.card.setCardBackgroundColor(h.itemView.getContext().getColor(android.R.color.white));
            h.card.setStrokeColor(h.itemView.getContext().getColor(R.color.gray_200));
            h.card.setStrokeWidth(2);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDriverClick(d);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DriverVH extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView tvName;
        final TextView tvEmail;
        final TextView tvStatus;

        DriverVH(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            tvName = itemView.findViewById(R.id.tvDriverName);
            tvEmail = itemView.findViewById(R.id.tvDriverEmail);
            tvStatus = itemView.findViewById(R.id.tvDriverStatus);
        }
    }
}