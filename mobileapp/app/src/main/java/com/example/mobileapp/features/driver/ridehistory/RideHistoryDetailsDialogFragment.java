package com.example.mobileapp.features.driver.ridehistory;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.models.Ride;
import com.example.mobileapp.features.shared.models.enums.RideStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RideHistoryDetailsDialogFragment extends DialogFragment {

    private static final String ARG_RIDE = "arg_ride"; // key which identificates ride in bundle

    // standard pattern for fragments: data goes in arguments, so fragment can be made by Android alone
    public static RideHistoryDetailsDialogFragment newInstance(@NonNull Ride ride) {
        RideHistoryDetailsDialogFragment f = new RideHistoryDetailsDialogFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_RIDE, ride);
        f.setArguments(b);
        return f;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        @SuppressLint("InflateParams") View v = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_ride_details_dialog, null, false);
        dialog.setContentView(v);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
        }

        Ride ride = requireArguments().getParcelable(ARG_RIDE);

        TextView tvPickup = v.findViewById(R.id.tv_pickup);
        TextView tvDropoff = v.findViewById(R.id.tv_dropoff);
        TextView tvStart = v.findViewById(R.id.tv_start);
        TextView tvEnd = v.findViewById(R.id.tv_end);
        TextView tvDuration = v.findViewById(R.id.tv_duration);
        TextView tvTotal = v.findViewById(R.id.tv_total);

        TextView status = v.findViewById(R.id.chip_status);
        TextView panic = v.findViewById(R.id.chip_panic);

        View cancellationLayout = v.findViewById(R.id.layout_cancellation);
        TextView tvCancelledBy = v.findViewById(R.id.tv_cancelled_by);
        TextView tvCancelReason = v.findViewById(R.id.tv_cancel_reason);
        TextView tvCancelTime = v.findViewById(R.id.tv_cancel_time);

        TextView tvPassengerName = v.findViewById(R.id.tv_passenger_name);
        TextView tvPassengerPhone = v.findViewById(R.id.tv_passenger_phone);

        View layoutPanicTime = v.findViewById(R.id.layout_panic_time);
        TextView tvPanicTime = v.findViewById(R.id.tv_panic_time);

        ImageButton btnCloseTop = v.findViewById(R.id.btn_close);
        View btnCloseBottom = v.findViewById(R.id.btn_close_bottom);

        if (ride != null) {
            tvPickup.setText(ride.getFrom());
            tvDropoff.setText(ride.getTo());

            // start/end
            tvStart.setText(ride.getDate() + " " + extractStart(ride.getTime()));
            tvEnd.setText(ride.getDate() + " " + extractEnd(ride.getTime()));
            tvDuration.setText(computeDuration(ride.getTime()));

            tvTotal.setText(ride.getPrice());

            // STATUS styling
            applyStatus(status, ride.getStatus());

            // PANIC styling
            if (ride.isPanic()) {
                panic.setText("Yes");
                panic.setBackgroundResource(R.drawable.bg_panic);
                panic.setTextColor(ContextCompat.getColor(requireContext(), R.color.panic_ride));

                layoutPanicTime.setVisibility(View.VISIBLE);
                tvPanicTime.setText(ride.getDate() + ", " + extractStart(ride.getTime()));
            } else {
                panic.setText("No");
                panic.setBackgroundResource(R.drawable.bg_neutral);
                panic.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral));

                layoutPanicTime.setVisibility(View.GONE);
            }

            // Cancellation block only if cancelled
            if (ride.getStatus() == RideStatus.CANCELLED) {
                cancellationLayout.setVisibility(View.VISIBLE);
                tvCancelledBy.setText(ride.getCancelledBy() != null ? ride.getCancelledBy() : "-");
                tvCancelReason.setText("Changed plans");
                tvCancelTime.setText(ride.getDate() + ", " + extractStart(ride.getTime()));
            } else {
                cancellationLayout.setVisibility(View.GONE);
            }

            tvPassengerName.setText("Jelena Nikolić");
            tvPassengerPhone.setText("+381 63 345 6789");
        }

        View.OnClickListener close = vv -> dismiss();
        btnCloseTop.setOnClickListener(close);
        btnCloseBottom.setOnClickListener(close);

        return dialog;
    }

    @SuppressLint("SetTextI18n")
    private void applyStatus(TextView chip, RideStatus st) {
        if (st == RideStatus.COMPLETED) {
            chip.setText("Completed");
            chip.setBackgroundResource(R.drawable.bg_completed);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.completed_ride));
        } else if (st == RideStatus.CANCELLED) {
            chip.setText("Cancelled");
            chip.setBackgroundResource(R.drawable.bg_cancelled);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.cancel_ride));
        } else if (st == RideStatus.ASSIGNED) {
            chip.setText("Assigned");
            chip.setBackgroundResource(R.drawable.bg_neutral);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral));
        } else {
            chip.setText("Scheduled");
            chip.setBackgroundResource(R.drawable.bg_neutral);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral));
        }
    }

    private String extractStart(String timeRange) {
        // "22:15 - 22:32" -> "22:15"
        if (timeRange == null) return "";
        String[] parts = timeRange.split("-");
        return parts.length > 0 ? parts[0].trim() : timeRange;
    }

    private String extractEnd(String timeRange) {
        // "22:15 - 22:32" -> "22:32"
        if (timeRange == null) return "";
        String[] parts = timeRange.split("-");
        return parts.length > 1 ? parts[1].trim() : "";
    }

    private String computeDuration(String timeRange) {
        if (timeRange == null) return "";
        String[] parts = timeRange.split("-");
        if (parts.length < 2) return "";
        String start = parts[0].trim();
        String end = parts[1].trim();
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            java.util.Date d1 = fmt.parse(start);
            java.util.Date d2 = fmt.parse(end);
            if (d1 == null || d2 == null) return "";
            long diffMin = (d2.getTime() - d1.getTime()) / (60 * 1000);
            return diffMin + " minutes";
        } catch (ParseException e) {
            return "";
        }
    }
}
