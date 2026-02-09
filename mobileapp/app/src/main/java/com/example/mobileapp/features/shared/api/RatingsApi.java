package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.RatingDto;
import com.example.mobileapp.features.shared.api.dto.RatingRequestDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RatingsApi {

    @POST("api/ratings")
    Call<RatingDto> submitRating(
            @Header("Authorization") String bearer,
            @Body RatingRequestDto request
    );

    @GET("api/ratings/ride/{rideId}")
    Call<List<RatingDto>> getRideRatings(
            @Header("Authorization") String bearer,
            @Path("rideId") int rideId
    );
}
