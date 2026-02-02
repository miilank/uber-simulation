package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.models.VehicleMarker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface VehiclesApi {
    @GET("api/vehicles/map")
    Call<List<VehicleMarker>> getMapVehicles();
}
