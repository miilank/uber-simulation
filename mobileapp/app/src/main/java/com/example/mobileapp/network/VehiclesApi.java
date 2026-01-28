package com.example.mobileapp.network;

import com.example.mobileapp.model.VehicleMarker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface VehiclesApi {
    @GET("api/vehicles/map")
    Call<List<VehicleMarker>> getMapVehicles();
}
