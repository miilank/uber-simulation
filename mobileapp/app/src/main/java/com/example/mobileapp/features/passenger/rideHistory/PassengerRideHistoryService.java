package com.example.mobileapp.features.passenger.rideHistory;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.dto.RideDetailDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerRideHistoryService {

    public interface DetailsCallback {
        void onSuccess(@NonNull RideDetailDto dto);
        void onError(@NonNull String message);
    }

    private final RidesApi api;
    private final SharedPreferences prefs;

    public PassengerRideHistoryService(@NonNull Context ctx) {
        api = ApiClient.get().create(RidesApi.class);
        prefs = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    @Nullable
    private String bearer() {
        String token = prefs.getString("jwt", null);
        if (token == null || token.trim().isEmpty()) return null;
        return "Bearer " + token;
    }

    public void fetchRideDetails(int rideId, @NonNull DetailsCallback cb) {
        String auth = bearer();
        if (auth == null) {
            cb.onError("Not authenticated");
            return;
        }

        api.getRideDetails(auth, rideId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RideDetailDto> call,
                                   @NonNull Response<RideDetailDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    cb.onError("Failed (" + response.code() + ")");
                    return;
                }
                cb.onSuccess(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<RideDetailDto> call,
                                  @NonNull Throwable t) {
                cb.onError("Network error");
            }
        });
    }
}
