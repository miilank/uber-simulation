package com.example.mobileapp.features.driver.ridehistory;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.LocationDto;
import com.example.mobileapp.features.shared.api.dto.PassengerDto;
import com.example.mobileapp.features.shared.api.dto.RideDetailDto;
import com.example.mobileapp.features.shared.api.dto.RideInconsistencyDto;
import com.example.mobileapp.features.shared.models.Ride;
import com.example.mobileapp.features.shared.models.enums.RideStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideHistoryDetailsDialogFragment extends DialogFragment {
    private DriverRideHistoryService detailsService;
    private InconsistencyAdapter incAdapter;
    private RecyclerView rvIncs;
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
        @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.fragment_ride_details_dialog, null, false);
        dialog.setContentView(v);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
        }

        Ride ride = requireArguments().getParcelable(ARG_RIDE);

        detailsService = new DriverRideHistoryService(requireContext());
        rvIncs = v.findViewById(R.id.rv_inconsistencies);
        rvIncs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvIncs.setNestedScrollingEnabled(false);
        incAdapter = new InconsistencyAdapter();
        rvIncs.setAdapter(incAdapter);

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
        }

        if (ride != null && ride.getId() != null) {
            detailsService.fetchRideDetails(ride.getId(), new DriverRideHistoryService.DetailsCallback() {
                @Override
                public void onSuccess(@NonNull RideDetailDto dto) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> bindDetails(v, dto));
                }

                @Override
                public void onError(@NonNull String message) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed: " + message, Toast.LENGTH_SHORT).show()
                    );
                }
            });
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
        } else if (st == RideStatus.STOPPED) {
            chip.setText("Stopped");
            chip.setBackgroundResource(R.drawable.bg_panic);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.panic_ride));
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
            Date d1 = fmt.parse(start);
            Date d2 = fmt.parse(end);
            if (d1 == null || d2 == null) return "";
            long diffMin = (d2.getTime() - d1.getTime()) / (60 * 1000);
            return diffMin + " minutes";
        } catch (ParseException e) {
            return "";
        }
    }

    @SuppressLint("SetTextI18n")
    private void bindDetails(@NonNull View v, @NonNull RideDetailDto d) {
        TextView tvPickup = v.findViewById(R.id.tv_pickup);
        LinearLayout wpRoot = v.findViewById(R.id.layout_waypoints);
        wpRoot.removeAllViews();

        if (d.waypoints != null) {
            for (int i = 0; i < d.waypoints.size(); i++) {
                LocationDto w = d.waypoints.get(i);
                View row = getLayoutInflater().inflate(R.layout.item_route_stop, wpRoot, false);
                ((TextView) row.findViewById(R.id.tv_label)).setText("Stop " + (i + 1));
                ((TextView) row.findViewById(R.id.tv_address)).setText(w.getAddress());
                wpRoot.addView(row);
            }
        }

        TextView tvDropoff = v.findViewById(R.id.tv_dropoff);
        TextView tvStart = v.findViewById(R.id.tv_start);
        TextView tvEnd = v.findViewById(R.id.tv_end);
        TextView tvDuration = v.findViewById(R.id.tv_duration);
        TextView tvTotal = v.findViewById(R.id.tv_total);
        TextView status = v.findViewById(R.id.chip_status);

        if (d.startAddress != null) tvPickup.setText(d.startAddress);
        if (d.endAddress != null) tvDropoff.setText(d.endAddress);

        String startTs = d.actualStartTime != null ? d.actualStartTime : d.estimatedStartTime;
        String endTs = d.actualEndTime != null ? d.actualEndTime : d.estimatedEndTime;

        if (startTs != null) tvStart.setText(formatDateTimeString(startTs));
        if (endTs != null) tvEnd.setText(formatDateTimeString(endTs));

        String dur = computeDurationFromIso(startTs, endTs);
        if (!dur.isEmpty()) tvDuration.setText(dur);

        if (d.status != null) {
            RideStatus st = parseRideStatus(d.status);
            applyStatus(status, st);
        }

        LinearLayout passengersRoot = v.findViewById(R.id.layout_passengers);
        passengersRoot.removeAllViews();

        if (d.passengers != null) {
            for (PassengerDto p : d.passengers) {
                if (p == null) continue;

                View row = getLayoutInflater().inflate(R.layout.item_passenger, passengersRoot, false);

                TextView tvName = row.findViewById(R.id.tvPassengerName);
                TextView tvPhone = row.findViewById(R.id.tvPassengerPhone);

                String name = (safe(p.firstName) + " " + safe(p.lastName)).trim();
                if (name.isEmpty()) name = safe(p.email);
                tvName.setText(name);

                String contact = safe(p.email);
                tvPhone.setText(!contact.isEmpty() ? contact : "-");

                passengersRoot.addView(row);
            }
        }

        // cancellation block
        View cancellationLayout = v.findViewById(R.id.layout_cancellation);
        TextView tvCancelledBy = v.findViewById(R.id.tv_cancelled_by);
        TextView tvCancelReason = v.findViewById(R.id.tv_cancel_reason);
        TextView tvCancelTime = v.findViewById(R.id.tv_cancel_time);

        if ("CANCELLED".equalsIgnoreCase(d.status)) {
            cancellationLayout.setVisibility(View.VISIBLE);
            tvCancelledBy.setText(nonEmptyOrDash(d.cancelledBy));
            tvCancelReason.setText(nonEmptyOrDash(d.cancellationReason));
            tvCancelTime.setText(nonEmptyOrDash(formatDateTimeString(d.cancellationTime)));
        } else {
            cancellationLayout.setVisibility(View.GONE);
        }

        // panic
        TextView panicChip = v.findViewById(R.id.chip_panic);
        View layoutPanicTime = v.findViewById(R.id.layout_panic_time);
        TextView tvPanicTime = v.findViewById(R.id.tv_panic_time);

        boolean panic = d.panicActivated != null && d.panicActivated;
        if (panic) {
            panicChip.setText("Yes");
            panicChip.setBackgroundResource(R.drawable.bg_panic);
            panicChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.panic_ride));
            layoutPanicTime.setVisibility(View.VISIBLE);
            tvPanicTime.setText(nonEmptyOrDash(formatDateTimeString(d.panicActivatedAt)));
        } else {
            panicChip.setText("No");
            panicChip.setBackgroundResource(R.drawable.bg_neutral);
            panicChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral));
            layoutPanicTime.setVisibility(View.GONE);
        }

        // total price
        if (d.totalPrice != null) {
            tvTotal.setText(String.format(Locale.getDefault(), "€%.2f", d.totalPrice));
        }

        // inconsistencies
        List<RideInconsistencyDto> incs = d.inconsistencies;
        boolean has = incs != null && !incs.isEmpty();

        TextView tvTitle = v.findViewById(R.id.tv_reports_title);
        tvTitle.setVisibility(has ? View.VISIBLE : View.GONE);
        rvIncs.setVisibility(has ? View.VISIBLE : View.GONE);

        if (has) {
            incAdapter.setItems(incs);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String nonEmptyOrDash(String s) {
        String v = safe(s).trim();
        return v.isEmpty() ? "-" : v;
    }

    private RideStatus parseRideStatus(String status) {
        if (status == null) return RideStatus.SCHEDULED;
        try {
            return RideStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return RideStatus.SCHEDULED;
        }
    }

    private String computeDurationFromIso(String startIso, String endIso) {
        Date s = parseIsoDateTime(startIso);
        Date e = parseIsoDateTime(endIso);
        if (s == null || e == null) return "";
        long diffMin = (e.getTime() - s.getTime()) / (60 * 1000);
        if (diffMin < 0) return "";
        return diffMin + " minutes";
    }

    private String formatDateTimeString(String iso) {
        Date d = parseIsoDateTime(iso);
        if (d == null) return "";
        return new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(d);
    }

    private Date parseIsoDateTime(String iso) {
        if (iso == null) return null;
        String s = iso.trim();
        if (s.isEmpty()) return null;

        Date d = tryParseIso(s, "yyyy-MM-dd'T'HH:mm:ss.SSSX");
        if (d != null) return d;

        d = tryParseIso(s, "yyyy-MM-dd'T'HH:mm:ssX");
        if (d != null) return d;

        d = tryParseIso(s, "yyyy-MM-dd'T'HH:mm:ss.SSS");
        if (d != null) return d;

        return tryParseIso(s, "yyyy-MM-dd'T'HH:mm:ss");
    }

    private Date tryParseIso(String s, String pattern) {
        try {
            SimpleDateFormat f = new SimpleDateFormat(pattern, Locale.US);
            f.setLenient(false);
            return f.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
