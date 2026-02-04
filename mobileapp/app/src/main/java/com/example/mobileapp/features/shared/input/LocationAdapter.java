package com.example.mobileapp.features.shared.input;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.GeocodeResult;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.Holder> {
    private List<GeocodeResult> results;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GeocodeResult result);
    }

    public LocationAdapter(List<GeocodeResult> results, OnItemClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location_result, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        GeocodeResult result = results.get(position);
        holder.primaryText.setText(result.display_name.split(",")[0]);
        holder.secondaryText.setText(result.display_name);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(result));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView primaryText, secondaryText;
        Holder(View itemView) {
            super(itemView);
            primaryText = itemView.findViewById(R.id.primary_text);
            secondaryText = itemView.findViewById(R.id.secondary_text);
        }
    }
}

