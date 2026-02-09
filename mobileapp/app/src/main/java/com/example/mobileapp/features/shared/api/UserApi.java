package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.core.api.dto.RegisterRequest;
import com.example.mobileapp.features.shared.api.dto.UserUpdateRequestDto;
import com.example.mobileapp.features.shared.models.User;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface UserApi {
    @GET("api/users/profile")
    Call<User> fetchMe();

    @Multipart
    @PUT("api/users/profile")
    Call<User> updateProfile(@Part("update") UserUpdateRequestDto user,
                        @Part MultipartBody.Part profileImage);

    // Using map to avoid making whole new class for 2 fields
    @PUT("api/users/change-password")
    Call<Void> changePassword(Map<String, String> request);
}
