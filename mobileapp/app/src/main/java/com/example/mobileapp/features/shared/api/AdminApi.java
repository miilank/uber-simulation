package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.DriverListItemDto;
import com.example.mobileapp.features.shared.api.dto.DriverRideDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface AdminApi {

    @GET("api/admin/drivers/all-with-status")
    Call<List<DriverListItemDto>> getAllDrivers(
            @Header("Authorization") String authHeader
    );

    @GET("api/admin/drivers/{driverEmail}/rides")
    Call<List<DriverRideDto>> getDriverRides(
            @Header("Authorization") String authHeader,
            @Path("driverEmail") String driverEmail
    );
}