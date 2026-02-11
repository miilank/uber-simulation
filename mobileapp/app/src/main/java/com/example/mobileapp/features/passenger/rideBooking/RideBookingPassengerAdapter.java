package com.example.mobileapp.features.passenger.rideBooking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;

import java.util.List;

public class RideBookingPassengerAdapter extends RecyclerView.Adapter<RideBookingPassengerAdapter.PassengerVH> {

    public interface OnChangedListener {
        void onPassengerChanged(int index, String newPassenger);
    }

    public interface OnDeletedListener {
        void onDeleted(int index);
    }

    private final List<String> items;
    private final OnChangedListener changedListener;
    private final OnDeletedListener deletedListener;

    public RideBookingPassengerAdapter(List<String> items, OnChangedListener changedListener, OnDeletedListener deletedListener) {
        this.items = items;
        this.changedListener = changedListener;
        this.deletedListener = deletedListener;
    }

    @NonNull
    @Override
    public PassengerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_passenger, parent, false);
        return new PassengerVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PassengerVH holder, int position) {
        String val = items.get(position);
        holder.passengerInput.setText(val);

        holder.passengerInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                String newVal = holder.passengerInput.getText().toString();
                items.set(pos, newVal);
                changedListener.onPassengerChanged(pos, newVal);
            }
        });

        holder.deleteButton.setOnClickListener((view) -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            deletedListener.onDeleted(pos);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static final class PassengerVH extends RecyclerView.ViewHolder {
        final AppCompatEditText passengerInput;
        final AppCompatImageButton deleteButton;

        public PassengerVH(@NonNull View itemView) {
            super(itemView);
            passengerInput = itemView.findViewById(R.id.passenger_input);
            deleteButton = itemView.findViewById(R.id.btn_delete_button);
        }
    }
}
