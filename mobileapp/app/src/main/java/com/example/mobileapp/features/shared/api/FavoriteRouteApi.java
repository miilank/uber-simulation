package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.FavoriteRouteDto;
import com.example.mobileapp.features.shared.api.dto.LocationDto;
import com.example.mobileapp.features.shared.api.dto.RideHistoryResponseDto;
import com.example.mobileapp.features.shared.models.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FavoriteRouteApi {
    @GET("api/favorite-routes")
    Call<List<FavoriteRouteDto>> getFavoriteRoutes();

    @DELETE("api/favorite-routes/{id}")
    Call<Void> deleteFavoriteRoute(@Path("id") int id);
}