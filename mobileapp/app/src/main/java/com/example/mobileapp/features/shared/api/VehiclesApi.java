package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.VehiclePositionUpdateDto;
import com.example.mobileapp.features.shared.models.VehicleMarker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface VehiclesApi {
    @GET("api/vehicles/map")
    Call<List<VehicleMarker>> getMapVehicles();

    @PUT("api/vehicles/{id}/position")
    Call<Void> updateVehiclePosition(
            @Header("Authorization") String bearerToken,
            @Path("id") int vehicleId,
            @Body VehiclePositionUpdateDto dto
    );
}
