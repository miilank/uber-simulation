package com.example.mobileapp.ui.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;

import java.util.List;

public class DriverDashboardAdapter extends RecyclerView.Adapter<DriverDashboardAdapter.BookedRideVH> {

    public enum RideStatus { SCHEDULED, ASSIGNED, STARTED }
    public enum Requirement { SEDAN, SUV, BABY, PETS, VAN }

    public static final class BookedRide {
        public final String date;
        public final String time;
        public final String from;
        public final String to;
        public final int passengers;
        public final List<Requirement> requirements;
        public final RideStatus status;

        public BookedRide(String date, String time, String from, String to, int passengers,
                          List<Requirement> requirements, RideStatus status) {
            this.date = date;
            this.time = time;
            this.from = from;
            this.to = to;
            this.passengers = passengers;
            this.requirements = requirements;
            this.status = status;
        }
    }

    private final List<BookedRide> items;

    public DriverDashboardAdapter(@NonNull List<BookedRide> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public BookedRideVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booked_ride, parent, false);
        return new BookedRideVH(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BookedRideVH h, int position) {
        BookedRide r = items.get(position);

        h.tvDate.setText(r.date);
        h.tvTime.setText(r.time);
        h.tvRoute.setText(r.from + "  →  " + r.to);
        h.tvPassengers.setText(String.valueOf(r.passengers));

        h.pillStatus.setText(statusText(r.status));
        h.pillStatus.setBackgroundResource(statusBg(r.status));

        h.reqContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(h.itemView.getContext());
        for (Requirement req : r.requirements) {
            View pill = inflater.inflate(R.layout.view_requirement, h.reqContainer, false);

            TextView tv = pill.findViewById(R.id.tvReq);
            tv.setText(reqIcon(req));
            tv.setBackgroundResource(reqBg(req));

            h.reqContainer.addView(pill);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String statusText(RideStatus s) {
        if (s == RideStatus.ASSIGNED) return "Assigned";
        if (s == RideStatus.STARTED) return "Started";
        return "Scheduled";
    }

    private static int statusBg(RideStatus s) {
        if (s == RideStatus.ASSIGNED) return R.drawable.bg_assigned;
        if (s == RideStatus.STARTED) return R.drawable.bg_started;
        return R.drawable.bg_scheduled;
    }

    private static String reqIcon(Requirement r) {
        switch (r) {
            case BABY: return "🍼";
            case SUV: return "🧭";
            case PETS: return "🐾";
            case VAN: return "🚐";
            default: return "🚘";
        }
    }

    private static int reqBg(Requirement r) {
        switch (r) {
            case BABY: return R.drawable.bg_req_baby;
            case SUV: return R.drawable.bg_req_suv;
            case PETS: return R.drawable.bg_req_pets;
            case VAN: return R.drawable.bg_req_van;
            default: return R.drawable.bg_req_sedan;
        }
    }

    public static final class BookedRideVH extends RecyclerView.ViewHolder {
        final TextView tvDate;
        final TextView tvTime;
        final TextView tvRoute;
        final TextView tvPassengers;
        final TextView pillStatus;
        final ViewGroup reqContainer;

        public BookedRideVH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvBookedDate);
            tvTime = itemView.findViewById(R.id.tvBookedTime);
            tvRoute = itemView.findViewById(R.id.tvBookedRoute);
            tvPassengers = itemView.findViewById(R.id.tvBookedPassengers);
            pillStatus = itemView.findViewById(R.id.pillBookedStatus);
            reqContainer = itemView.findViewById(R.id.requirementsContainer);
        }
    }
}
