package com.example.mobileapp.features.passenger.currentride;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RatingsApi;
import com.example.mobileapp.features.shared.api.dto.RatingDto;
import com.example.mobileapp.features.shared.api.dto.RatingRequestDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RatingDialogFragment extends DialogFragment {

    private static final String ARG_RIDE_ID = "rideId";

    public interface Listener {
        void onRatingSubmitted();
    }

    public static RatingDialogFragment newInstance(int rideId) {
        RatingDialogFragment f = new RatingDialogFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_RIDE_ID, rideId);
        f.setArguments(b);
        return f;
    }

    private Listener listener;

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    private int driverRating = 0;
    private int vehicleRating = 0;

    @Override
    public void onStart() {
        super.onStart();

        Dialog d = getDialog();
        if (d == null || d.getWindow() == null) return;

        int screenW = requireContext().getResources().getDisplayMetrics().widthPixels;

        int w = (int) (screenW * 0.96f);

        d.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);

        int side = dp(8);
        d.getWindow().getDecorView().setPadding(side, dp(8), side, dp(8));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog d = new Dialog(requireContext());
        View v = getLayoutInflater().inflate(R.layout.fragment_rating_dialog, null, false);
        d.setContentView(v);

        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        int rideId = requireArguments().getInt(ARG_RIDE_ID, -1);
        if (rideId <= 0) {
            dismiss();
            return d;
        }

        LinearLayout rowDriver = v.findViewById(R.id.rowDriverStars);
        LinearLayout rowVehicle = v.findViewById(R.id.rowVehicleStars);

        EditText etComment = v.findViewById(R.id.etComment);
        TextView tvCount = v.findViewById(R.id.tvCount);

        TextView btnCancel = v.findViewById(R.id.btnCancel);
        TextView btnSubmit = v.findViewById(R.id.btnSubmit);

        // build stars
        buildStars(rowDriver, true, btnSubmit);
        buildStars(rowVehicle, false, btnSubmit);

        // char count
        updateCount(tvCount, etComment.getText());
        etComment.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCount(tvCount, s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(vv -> dismiss());

        btnSubmit.setOnClickListener(vv -> {
            if (!isValid()) return;

            btnSubmit.setEnabled(false);
            btnSubmit.setAlpha(0.6f);
            btnSubmit.setText("Sending...");

            SharedPreferences prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
            String token = prefs.getString("jwt", null);
            if (token == null || token.isEmpty()) {
                Toast.makeText(requireContext(), "Missing token", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            String comment = etComment.getText() != null ? etComment.getText().toString().trim() : null;

            RatingsApi api = ApiClient.get().create(RatingsApi.class);
            RatingRequestDto req = new RatingRequestDto(
                    rideId,
                    vehicleRating,
                    driverRating,
                    comment
            );

            api.submitRating("Bearer " + token, req).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RatingDto> call, @NonNull Response<RatingDto> response) {
                    if (!response.isSuccessful()) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setAlpha(1f);
                        btnSubmit.setText("Submit");
                        if (response.code() == 500) {
                            Toast.makeText(requireContext(), "The ride has already been rated.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to submit rating", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (listener != null) listener.onRatingSubmitted();
                    dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<RatingDto> call, @NonNull Throwable t) {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setAlpha(1f);
                    btnSubmit.setText("Submit");
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // initial submit state
        syncSubmit(btnSubmit);

        return d;
    }

    private void buildStars(@NonNull LinearLayout row, boolean isDriver, @NonNull TextView btnSubmit) {
        row.removeAllViews();

        int sizeSp = 28;
        int padDp = 4;

        for (int i = 1; i <= 5; i++) {
            final int star = i;

            TextView tv = new TextView(requireContext());
            tv.setText("★");
            tv.setTextSize(sizeSp);
            tv.setPadding(dp(padDp), 0, dp(padDp), 0);

            tv.setOnClickListener(v -> {
                if (isDriver) driverRating = star;
                else vehicleRating = star;

                refreshRow(row, isDriver ? driverRating : vehicleRating);
                syncSubmit(btnSubmit);
            });

            row.addView(tv);
        }

        // initial color
        refreshRow(row, 0);
    }

    private void refreshRow(@NonNull LinearLayout row, int selected) {
        int on = ContextCompat.getColor(requireContext(), R.color.app_accent);
        int off = 0xFFD1D5DB; // gray-300

        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor((i + 1) <= selected ? on : off);
            }
        }
    }

    private boolean isValid() {
        return driverRating > 0 && vehicleRating > 0;
    }

    private void syncSubmit(@NonNull TextView btnSubmit) {
        boolean ok = isValid();
        btnSubmit.setEnabled(ok);
        btnSubmit.setAlpha(ok ? 1f : 0.6f);
    }

    private void updateCount(@NonNull TextView tv, @Nullable CharSequence s) {
        int len = (s == null) ? 0 : s.length();
        tv.setText(len + "/500");
    }

    private int dp(int v) {
        float d = requireContext().getResources().getDisplayMetrics().density;
        return (int) (v * d);
    }
}
