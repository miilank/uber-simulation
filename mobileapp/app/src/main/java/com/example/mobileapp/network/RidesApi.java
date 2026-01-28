package com.example.mobileapp.network;

import com.example.mobileapp.network.dto.RideHistoryResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
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
}
