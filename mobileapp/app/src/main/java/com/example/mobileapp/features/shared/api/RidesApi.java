package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.DriverRideDto;
import com.example.mobileapp.features.shared.api.dto.RideHistoryResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface RidesApi {

    @GET("api/rides/history")
    Call<RideHistoryResponseDto> getRideHistory(
            @Query("driverId") int driverId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    @GET("api/rides")
    Call<List<DriverRideDto>> getDriverRides(
            @Header("Authorization") String authHeader
    );
}
