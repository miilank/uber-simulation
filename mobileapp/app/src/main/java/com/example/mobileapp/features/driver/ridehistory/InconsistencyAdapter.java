package com.example.mobileapp.features.driver.ridehistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.RideInconsistencyDto;

import java.util.ArrayList;
import java.util.List;

public class InconsistencyAdapter extends RecyclerView.Adapter<InconsistencyAdapter.VH> {
    private final List<RideInconsistencyDto> items = new ArrayList<>();

    public void setItems(@Nullable List<RideInconsistencyDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InconsistencyAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inconsistency, parent, false);
        return new InconsistencyAdapter.VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InconsistencyAdapter.VH h, int pos) {
        RideInconsistencyDto d = items.get(pos);
        h.name.setText(d.passengerName != null ? d.passengerName : "-");
        h.time.setText(d.createdAt != null ? d.createdAt.replace("T"," ").substring(0, Math.min(16, d.createdAt.length())) : "");
        h.desc.setText(d.description != null ? d.description : "");
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, time, desc;

        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            time = v.findViewById(R.id.tv_time);
            desc = v.findViewById(R.id.tv_desc);
        }
    }
}
