package com.example.mobileapp.features.passenger.favoriteRoutes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.FavoriteRouteDto;
import com.example.mobileapp.features.shared.api.dto.LocationDto;

import java.util.List;


public class FavoriteRouteDetailsDialogFragment extends DialogFragment {
    private static final String ARG_ROUTE = "arg_route";
    public static final String FRAG_RESULT_KEY = "fav_route_delete";
    public static final String RESULT_ROUTE_ID = "route_id";
    public static FavoriteRouteDetailsDialogFragment newInstance(@NonNull FavoriteRouteDto route) {
        FavoriteRouteDetailsDialogFragment f = new FavoriteRouteDetailsDialogFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_ROUTE, route);
        f.setArguments(b);
        return f;
    }

    public FavoriteRouteDetailsDialogFragment() {}

    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        @SuppressLint("InflateParams") View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fragment_favorite_route_details, null, false);
        dialog.setContentView(v);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
        }

        FavoriteRouteDto route = requireArguments().getParcelable(ARG_ROUTE);
        TextView tvPickup = v.findViewById(R.id.tv_pickup);
        TextView tvDropoff = v.findViewById(R.id.tv_dropoff);
        LinearLayout llWaypoints = v.findViewById(R.id.ll_waypoints);

        ImageButton btnCloseTop = v.findViewById(R.id.btn_close);
        AppCompatButton btnDelete = v.findViewById(R.id.btn_delete_button);
        AppCompatButton btnCloseBottom = v.findViewById(R.id.btn_close_bottom);

        String pickupAddr = (route.getStartLocation() != null && route.getStartLocation().getAddress() != null)
                ? route.getStartLocation().getAddress() : "Error when fetching pickup.";
        String dropoffAddr = (route.getEndLocation() != null && route.getEndLocation().getAddress() != null)
                ? route.getEndLocation().getAddress() : "Error when fetching drop-off.";

        tvPickup.setText(pickupAddr);
        tvDropoff.setText(dropoffAddr);

        populateWaypoints(llWaypoints, route.getWaypoints());

        // handlers
        View.OnClickListener close = __ -> dismiss();
        btnCloseTop.setOnClickListener(close);
        btnCloseBottom.setOnClickListener(close);

        btnDelete.setOnClickListener(__ -> {
            Integer id = route.getId();
            Bundle result = new Bundle();
            result.putInt(RESULT_ROUTE_ID, id == null ? -1 : id);
            getParentFragmentManager().setFragmentResult(FRAG_RESULT_KEY, result);
            dismiss();
        });

        return dialog;
    }

    private void populateWaypoints(@NonNull LinearLayout container, @Nullable List<LocationDto> waypoints) {
        container.removeAllViews();
        if (waypoints == null || waypoints.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);

        Context ctx = requireContext();

        for (LocationDto wp : waypoints) {
            if (wp == null) continue;
            String address = wp.getAddress();
            if (address == null || address.trim().isEmpty()) continue;

            TextView tv = new TextView(ctx);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(6, 4, 0, 4);
            tv.setLayoutParams(lp);

            tv.setText(address);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tv.setTextColor(ContextCompat.getColor(ctx, R.color.neutral));

            tv.setMaxLines(1);
            tv.setEllipsize(TextUtils.TruncateAt.END);

            container.addView(tv);
        }
    }
}
