package com.example.mobileapp.features.passenger.favoriteRoutes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.FavoriteRouteDto;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRoutesAdapter extends RecyclerView.Adapter<FavoriteRoutesAdapter.FavoriteRouteVH> {
    public interface OnRouteClickListener {
        void onRouteClick(FavoriteRouteDto route);
    }

    public enum Requirement { LUXURY, STANDARD, BABY, PETS, VAN }

    private final List<FavoriteRouteDto> items;
    private final OnRouteClickListener listener;

    public FavoriteRoutesAdapter(@NonNull List<FavoriteRouteDto> items, OnRouteClickListener clickListener) {
        this.items = items;
        this.listener = clickListener;
    }


    @NonNull
    @Override
    public FavoriteRoutesAdapter.FavoriteRouteVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_route, parent, false);
        return new FavoriteRoutesAdapter.FavoriteRouteVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteRoutesAdapter.FavoriteRouteVH holder, int position) {
        FavoriteRouteDto route = items.get(position);
        holder.tvOrigin.setText(route.getStartLocation().getAddress());
        holder.tvDestination.setText(route.getEndLocation().getAddress());
        holder.tvName.setText(route.getName());
        ArrayList<Requirement> requirements = new ArrayList<>();
        if(route.isBabyFriendly()) requirements.add(Requirement.BABY);
        if(route.isPetsFriendly()) requirements.add(Requirement.PETS);
        if(route.getVehicleType() != null) {
            switch (route.getVehicleType()) {
                case STANDARD: requirements.add(Requirement.STANDARD);
                case LUXURY: requirements.add(Requirement.LUXURY);
                case VAN: requirements.add(Requirement.VAN);
            }
        }

        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        for (Requirement req : requirements) {
            View pill = inflater.inflate(R.layout.view_requirement, holder.reqContainer, false);

            TextView tv = pill.findViewById(R.id.tvReq);
            tv.setText(reqIcon(req));
            tv.setBackgroundResource(reqBg(req));

            holder.reqContainer.addView(pill);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRouteClick(route);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String reqIcon(Requirement r) {
        switch (r) {
            case BABY: return "Baby 🍼";
            case STANDARD: return "Standard 🧭";
            case PETS: return "Pet 🐾";
            case VAN: return "Van 🚐";
            case LUXURY: return "Luxury 🚘";
            default: return "";
        }
    }

    private static int reqBg(Requirement r) {
        switch (r) {
            case BABY: return R.drawable.bg_req_baby;
            case LUXURY: return R.drawable.bg_req_suv;
            case PETS: return R.drawable.bg_req_pets;
            case VAN: return R.drawable.bg_req_van;
            default: return R.drawable.bg_req_sedan;
        }
    }

    public static final class FavoriteRouteVH extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvOrigin;
        final TextView tvDestination;
        final ViewGroup reqContainer;

        public FavoriteRouteVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
            tvDestination = itemView.findViewById(R.id.tv_destination);
            reqContainer = itemView.findViewById(R.id.requirementsContainer);
        }
    }
}
