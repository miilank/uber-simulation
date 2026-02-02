package com.example.mobileapp.core.api;

import com.example.mobileapp.core.api.dto.LoginRequest;
import com.example.mobileapp.core.api.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
