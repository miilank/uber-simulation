package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.PricingConfigDto;
import com.example.mobileapp.features.shared.api.dto.PricingUpdateRequestDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PricingApi {

    @GET("api/pricing")
    Call<List<PricingConfigDto>> getAllPricing();

    @PUT("api/pricing/{vehicleType}")
    Call<PricingConfigDto> updatePricing(
            @Path("vehicleType") String vehicleType,
            @Body PricingUpdateRequestDto request
    );
}