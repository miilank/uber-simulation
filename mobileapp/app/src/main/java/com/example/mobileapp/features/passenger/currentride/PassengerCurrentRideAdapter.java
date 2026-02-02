package com.example.mobileapp.features.passenger.currentride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.models.PassengerItem;

import java.util.List;

/**
 * Adapter for passengers list shown on Current Ride screen.
 */
public class PassengerCurrentRideAdapter
        extends RecyclerView.Adapter<PassengerCurrentRideAdapter.PassengerViewHolder> {

    private final List<PassengerItem> passengers;

    public PassengerCurrentRideAdapter(@NonNull List<PassengerItem> passengers) {
        this.passengers = passengers;
    }

    @NonNull
    @Override
    public PassengerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_passenger_simple, parent, false);
        return new PassengerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PassengerViewHolder holder, int position) {
        PassengerItem p = passengers.get(position);
        holder.tvName.setText(p.getName());
        holder.tvRole.setText(p.getRole());
    }

    @Override
    public int getItemCount() {
        return passengers.size();
    }

    public static class PassengerViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvRole;

        PassengerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPassengerName);
            tvRole = itemView.findViewById(R.id.tvPassengerRole);
        }
    }
}
