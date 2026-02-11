package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.CreateRideRequestDto;
import com.example.mobileapp.features.shared.api.dto.DriverRideDto;
import com.example.mobileapp.features.shared.api.dto.HistoryReportDto;
import com.example.mobileapp.features.shared.api.dto.PassengerRideDto;
import com.example.mobileapp.features.shared.api.dto.PriceEstimateResponse;
import com.example.mobileapp.features.shared.api.dto.RideDetailDto;
import com.example.mobileapp.features.shared.api.dto.RideDto;
import com.example.mobileapp.features.shared.api.dto.RideEstimateRequest;
import com.example.mobileapp.features.shared.api.dto.RideHistoryResponseDto;
import com.example.mobileapp.features.shared.api.dto.RideInconsistencyRequestDto;
import com.example.mobileapp.features.shared.api.dto.RidePanicDto;

import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RidesApi {
    @POST("api/rides")
    Call<RideDto> requestRide(
            @Body CreateRideRequestDto request
            );

    @GET("api/rides/history")
    Call<RideHistoryResponseDto> getRideHistory(
            @Header("Authorization") String authHeader,
            @Query("driverId") int driverId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    @GET("api/rides/history-report")
    Call<HistoryReportDto> getHistoryReport(
            @Query("from") LocalDate from,
            @Query("to") LocalDate to,
            @Query("uuid") Integer uuid
    );

    @GET("api/drivers/rides/{rideId}/details")
    Call<RideDetailDto> getRideDetails(
            @Header("Authorization") String authHeader,
            @Path("rideId") int rideId
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

    @PUT("api/rides/{rideId}/complete")
    Call<DriverRideDto> completeRide(
            @Header("Authorization") String bearer,
            @Path("rideId") Integer rideId);

    @POST("api/rides/estimate")
    Call<PriceEstimateResponse> estimateRide(@Body RideEstimateRequest request);

    @POST("api/rides/{rideId}/inconsistency")
    Call<Void> reportInconsistency(
            @Header("Authorization") String bearerToken,
            @Path("rideId") int rideId,
            @Body RideInconsistencyRequestDto request
    );

    @GET("api/rides/{id}/eta")
    Call<com.example.mobileapp.features.shared.api.dto.RideEtaDto> getRideEta(
            @Header("Authorization") String bearerToken,
            @Path("id") int rideId
    );

    @PUT("api/rides/{id}/arrived-pickup")
    Call<Void> arrivedAtPickup(
            @Header("Authorization") String bearerToken,
            @Path("id") int rideId
    );

    @POST("api/rides/{id}/panic")
    Call<Void> panic(
            @Header("Authorization") String bearerToken,
            @Path("id") int rideId,
            @Body RidePanicDto request
    );
}
