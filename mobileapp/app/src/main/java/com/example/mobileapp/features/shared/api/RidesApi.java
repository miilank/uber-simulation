package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.DriverRideDto;
import com.example.mobileapp.features.shared.api.dto.PassengerRideDto;
import com.example.mobileapp.features.shared.api.dto.RideHistoryResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
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

    @GET("/api/rides/current-in-progress")
    Call<PassengerRideDto> getPassengerCurrentInProgress(@Header("Authorization") String bearerToken);

    @PUT("api/rides/{rideId}/start")
    Call<DriverRideDto> startRide(
            @Header("Authorization") String authHeader,
            @Path("rideId") int rideId
    );
}
