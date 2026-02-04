package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.GeocodeResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingApi {
    @GET("search")
    Call<List<GeocodeResult>> searchAddress(
            @Query("q") String query,
            @Query("format") String format,
            @Query("limit") int limit,
            @Query("addressdetails") int details
    );
}

