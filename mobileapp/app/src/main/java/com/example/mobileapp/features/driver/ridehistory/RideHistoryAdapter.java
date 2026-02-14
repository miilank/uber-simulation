package com.example.mobileapp.features.driver.ridehistory;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.models.Ride;
import com.example.mobileapp.features.shared.models.enums.RideStatus;

import java.util.List;

public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder> {

    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    private final List<Ride> rides;
    private final OnRideClickListener listener;

    public RideHistoryAdapter(@NonNull List<Ride> rides, OnRideClickListener listener) {
        this.rides = rides;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
        return new RideViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RideViewHolder h, int position) {
        Ride r = rides.get(position);

        h.tvDate.setText(r.getDate());
        h.tvTime.setText(r.getTime());
        h.tvPrice.setText(r.getPrice());
        h.tvOrigin.setText(r.getFrom());
        h.tvDestination.setText(r.getTo());

        // colors from values
        int neutralText = ContextCompat.getColor(h.itemView.getContext(), R.color.neutral);
        int neutralBg   = ContextCompat.getColor(h.itemView.getContext(), R.color.neutral_bckg);

        int completedText = ContextCompat.getColor(h.itemView.getContext(), R.color.completed_ride);
        int completedBg   = ContextCompat.getColor(h.itemView.getContext(), R.color.completed_ride_bckg);

        int cancelledText = ContextCompat.getColor(h.itemView.getContext(), R.color.cancel_ride);
        int cancelledBg   = ContextCompat.getColor(h.itemView.getContext(), R.color.cancel_ride_bckg);

        int panicText = ContextCompat.getColor(h.itemView.getContext(), R.color.panic_ride);
        int panicBg   = ContextCompat.getColor(h.itemView.getContext(), R.color.panic_ride_bckg);

        // RESET (bcs of recycling)
        h.chipStatus.setBackgroundResource(R.drawable.bg_neutral);
        h.chipStatus.setBackgroundTintList(ColorStateList.valueOf(neutralBg));
        h.chipStatus.setTextColor(neutralText);
        h.chipStatus.setText("Scheduled");

        h.chipPanic.setBackgroundResource(R.drawable.bg_neutral);
        h.chipPanic.setBackgroundTintList(ColorStateList.valueOf(neutralBg));
        h.chipPanic.setTextColor(neutralText);
        h.chipPanic.setText("No panic");

        // 2) STATUS
        RideStatus st = r.getStatus();
        if (st == RideStatus.COMPLETED) {
            h.chipStatus.setText("Completed");
            h.chipStatus.setBackgroundTintList(ColorStateList.valueOf(completedBg));
            h.chipStatus.setTextColor(completedText);

        } else if (st == RideStatus.CANCELLED) {
            h.chipStatus.setText("Cancelled");
            h.chipStatus.setBackgroundTintList(ColorStateList.valueOf(cancelledBg));
            h.chipStatus.setTextColor(cancelledText);

        } else if (st == RideStatus.ASSIGNED) {
            h.chipStatus.setText("Assigned");
            h.chipStatus.setBackgroundTintList(ColorStateList.valueOf(neutralBg));
            h.chipStatus.setTextColor(neutralText);

        } else { // SCHEDULED
            h.chipStatus.setText("Scheduled");
            h.chipStatus.setBackgroundTintList(ColorStateList.valueOf(neutralBg));
            h.chipStatus.setTextColor(neutralText);
        }

        // PANIC
        if (r.isPanic()) {
            h.chipPanic.setText("Panic");
            h.chipPanic.setBackgroundTintList(ColorStateList.valueOf(panicBg));
            h.chipPanic.setTextColor(panicText);
        }

        // CLICK
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRideClick(r);
        });

        h.btnRate.setVisibility(View.GONE);
    }


    @Override
    public int getItemCount() {
        return rides.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDate, tvTime, tvPrice, tvOrigin, tvDestination;
        final TextView chipStatus, chipPanic;
        final TextView btnRate;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
            tvDestination = itemView.findViewById(R.id.tv_destination);
            btnRate = itemView.findViewById(R.id.btn_rate);

            chipStatus = itemView.findViewById(R.id.chip_status);
            chipPanic = itemView.findViewById(R.id.chip_panic);
        }
    }
}
