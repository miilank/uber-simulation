package com.example.mobileapp.core.api;

import com.example.mobileapp.core.api.dto.LoginRequest;
import com.example.mobileapp.core.api.dto.LoginResponse;
import com.example.mobileapp.core.api.dto.RegisterRequest;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AuthApi {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @Multipart
    @POST("api/auth/register")
    Call<Void> register(@Part("user") RegisterRequest user,
                        @Part MultipartBody.Part profileImage);
}
