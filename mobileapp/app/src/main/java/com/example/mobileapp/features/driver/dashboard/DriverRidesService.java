package com.example.mobileapp.features.driver.dashboard;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.dto.DriverRideDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRidesService {

    private final RidesApi ridesApi;
    private final SharedPreferences prefs;

    private final MutableLiveData<List<DriverRideDto>> rides = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<DriverRideDto> currentRide = new MutableLiveData<>(null);
    private final MutableLiveData<List<DriverRideDto>> bookedRides = new MutableLiveData<>(Collections.emptyList());

    public DriverRidesService(@NonNull Context context) {
        this.ridesApi = ApiClient.get().create(RidesApi.class);
        this.prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public LiveData<List<DriverRideDto>> rides() {
        return rides;
    }

    public LiveData<DriverRideDto> currentRide() {
        return currentRide;
    }

    public LiveData<List<DriverRideDto>> bookedRides() {
        return bookedRides;
    }

    public void fetchRides() {
        String token = prefs.getString("jwt", null);
        if (token == null || token.isEmpty()) {
            rides.postValue(Collections.emptyList());
            currentRide.postValue(null);
            bookedRides.postValue(Collections.emptyList());
            return;
        }

        ridesApi.getDriverRides("Bearer " + token).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<DriverRideDto>> call,
                                   @NonNull Response<List<DriverRideDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    rides.postValue(Collections.emptyList());
                    currentRide.postValue(null);
                    bookedRides.postValue(Collections.emptyList());
                    return;
                }

                List<DriverRideDto> list = response.body();
                if (list == null) list = Collections.emptyList();

                rides.postValue(list);
                recompute(list);
            }

            @Override
            public void onFailure(@NonNull Call<List<DriverRideDto>> call, @NonNull Throwable t) {
                rides.postValue(Collections.emptyList());
                currentRide.postValue(null);
                bookedRides.postValue(Collections.emptyList());
            }
        });
    }

    private void recompute(@NonNull List<DriverRideDto> list) {
        DriverRideDto next = pickNextRide(list);
        currentRide.postValue(next);

        List<DriverRideDto> booked = new ArrayList<>();
        for (DriverRideDto r : list) {
            if (r == null) continue;
            if (next != null && next.id != null && r.id != null && r.id.equals(next.id)) continue;
            booked.add(r);
        }

        booked.sort(Comparator.comparingLong(r -> safeTimeMillis(r == null ? null : r.scheduledTime)));
        bookedRides.postValue(booked);
    }

    private DriverRideDto pickNextRide(@NonNull List<DriverRideDto> list) {
        List<DriverRideDto> inProgress = new ArrayList<>();
        List<DriverRideDto> accepted = new ArrayList<>();

        for (DriverRideDto r : list) {
            if (r == null || r.status == null) continue;

            if ("IN_PROGRESS".equals(r.status)) {
                inProgress.add(r);
            } else if ("ACCEPTED".equals(r.status)) {
                accepted.add(r);
            }
        }

        if (!inProgress.isEmpty()) {
            inProgress.sort(Comparator.comparingLong(r -> safeTimeMillis(r.scheduledTime)));
            return inProgress.get(0);
        }

        if (!accepted.isEmpty()) {
            accepted.sort(Comparator.comparingLong(r -> safeTimeMillis(r.scheduledTime)));
            return accepted.get(0);
        }

        return null;
    }

    private long safeTimeMillis(String iso) {
        if (iso == null) return Long.MAX_VALUE;

        try {
            String s = iso;

            int dot = s.indexOf('.');
            if (dot != -1) s = s.substring(0, dot);

            String[] parts = s.split("T");
            if (parts.length != 2) return Long.MAX_VALUE;

            String[] ymd = parts[0].split("-");
            String[] hms = parts[1].split(":");
            if (ymd.length != 3 || hms.length < 2) return Long.MAX_VALUE;

            int y = Integer.parseInt(ymd[0]);
            int m = Integer.parseInt(ymd[1]);
            int d = Integer.parseInt(ymd[2]);

            int hh = Integer.parseInt(hms[0]);
            int mm = Integer.parseInt(hms[1]);
            int ss = (hms.length >= 3) ? Integer.parseInt(hms[2]) : 0;

            long key = 0;
            key += (long) y * 10000000000L;
            key += (long) m * 100000000L;
            key += (long) d * 1000000L;
            key += (long) hh * 10000L;
            key += (long) mm * 100L;
            key += ss;

            return key;
        } catch (Exception ignored) {
            return Long.MAX_VALUE;
        }
    }
}
