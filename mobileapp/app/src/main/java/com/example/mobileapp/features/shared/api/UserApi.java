package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.core.api.dto.RegisterRequest;
import com.example.mobileapp.features.shared.api.dto.UserUpdateRequestDto;
import com.example.mobileapp.features.shared.models.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UserApi {
    @GET("api/users/profile")
    Call<User> fetchMe();

    @Multipart
    @PUT("api/users/profile")
    Call<User> updateProfile(@Part("update") UserUpdateRequestDto user,
                        @Part MultipartBody.Part profileImage);

    // Using map to avoid making whole new class for 2 fields
    @PUT("api/users/change-password")
    Call<Void> changePassword(@Body Map<String, String> request);

    @GET("/api/users")
    Call<List<User>> searchUsers(@Query("search") String searchString,
                                 @Query("pageSize") Integer pageSize,
                                 @Query("pageNumber") Integer pageNumber);

    @PUT("/api/users/block")
    Call<Void> blockUser(@Query("uuid") Integer id,
                         @Body RequestBody body);

    @PUT("/api/users/unblock")
    Call<Void> unblockUser(@Query("uuid") Integer id);
}
