package com.example.mobileapp.features.passenger.rideBooking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.LocationDto;
import com.example.mobileapp.features.shared.input.LocationSearchInputView;

import java.util.List;

public class RideBookingLocationAdapter extends RecyclerView.Adapter<RideBookingLocationAdapter.LocationVH> {

    public interface OnSelectedListener {
        void onSelected(int index, LocationDto selectedAddress);
    }

    public interface OnDeletedListener {
        void onDeleted(int index);
    }

    private final List<LocationDto> items;
    private final OnSelectedListener selectedListener;
    private final OnDeletedListener deletedListener;

    public RideBookingLocationAdapter(List<LocationDto> items, OnSelectedListener selectedListener, OnDeletedListener deletedListener) {
        this.items = items;
        this.selectedListener = selectedListener;
        this.deletedListener = deletedListener;
    }

    @NonNull
    @Override
    public LocationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location_search, parent, false);
        return new LocationVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationVH holder, int position) {
        LocationDto dto = items.get(position);
        String addr = dto == null ? "" : dto.getAddress();
        holder.addressInput.setAddress(addr);

        holder.addressInput.setOnLocationSelectedListener((geocodeResult) -> {
            int pos = holder.getBindingAdapterPosition(); // Proveri
            if (pos == RecyclerView.NO_POSITION) return;

            geocodeResult.formatAddress();
            LocationDto selected = new LocationDto();
            selected.setAddress(geocodeResult.formattedResult);
            selected.setLatitude(geocodeResult.lat);
            selected.setLongitude(geocodeResult.lon);

            selectedListener.onSelected(pos, selected);
        });

        holder.deleteButton.setOnClickListener((view) -> {
            int pos = holder.getBindingAdapterPosition(); // Proveri
            if (pos == RecyclerView.NO_POSITION) return;

            deletedListener.onDeleted(pos);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static final class LocationVH extends RecyclerView.ViewHolder {
        final LocationSearchInputView addressInput;
        final AppCompatImageButton deleteButton;

        public LocationVH(@NonNull View itemView) {
            super(itemView);
            addressInput = itemView.findViewById(R.id.address_input);
            deleteButton = itemView.findViewById(R.id.btn_delete_button);
        }
    }
}
