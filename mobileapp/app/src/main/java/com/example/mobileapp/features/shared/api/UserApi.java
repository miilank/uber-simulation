package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.models.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserApi {
    @GET("api/users/profile")
    Call<User> fetchMe(@Header("Authorization") String authHeader);
}
