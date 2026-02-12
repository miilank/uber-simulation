package com.example.mobileapp.features.shared.api;

import com.example.mobileapp.features.shared.api.dto.NotificationDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationApi {

    @GET("api/notifications")
    Call<List<NotificationDto>> getNotifications();

    @PUT("api/notifications/{notificationId}/read")
    Call<Void> markAsRead(@Path("notificationId") Integer notificationId);

    @GET("api/notifications/unread-count")
    Call<Long> getUnreadCount();
}