package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.DriverCreationDto;
import com.example.mobileapp.features.shared.api.dto.DriverDto;
import com.example.mobileapp.features.shared.api.dto.DriverUpdateDto;
import com.example.mobileapp.features.shared.api.dto.UserUpdateRequestDto;

import java.net.URI;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface DriversApi {
    @GET("api/drivers/profile")
    Call<DriverDto> fetchMe();

    @Multipart
    @PUT("api/drivers/profile")
    Call<Void> updateProfile(@Part("update") UserUpdateRequestDto user,
                             @Part MultipartBody.Part profileImage);

    @GET("/api/drivers/pending-updates")
    Call<List<DriverUpdateDto>> getPendingChanges();

    @PUT("api/drivers/{driverId}/approve-update")
    Call<Void> approveUpdate(@Path("driverId") int driverId);

    @PUT("api/drivers/{driverId}/reject-update")
    Call<Void> rejectUpdate(@Path("driverId") int driverId);


    @Multipart
    @POST("/api/drivers")
    Call<Void> createDriver(@Part("user") DriverCreationDto request,
                            @Part MultipartBody.Part profileImage);
}
