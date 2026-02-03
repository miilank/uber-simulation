package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.OsrmRouteResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RoutingApi {
    @GET("route/v1/driving/{coords}")
    Call<OsrmRouteResponse> route(
            @Path(value = "coords", encoded = true) String coords,
            @Query("overview") String overview,
            @Query("geometries") String geometries
    );
}
