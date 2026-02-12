package com.example.mobileapp.features.shared.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.NotificationApi;
import com.example.mobileapp.features.shared.api.dto.NotificationDto;
import com.example.mobileapp.features.shared.models.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private static NotificationRepository instance;
    private final NotificationApi api;

    private final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> unreadCountLiveData = new MutableLiveData<>(0);

    private NotificationRepository() {
        this.api = ApiClient.get().create(NotificationApi.class);
    }

    public static synchronized NotificationRepository getInstance() {
        if (instance == null) {
            instance = new NotificationRepository();
        }
        return instance;
    }

    public LiveData<List<Notification>> getNotifications() {
        return notificationsLiveData;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCountLiveData;
    }

    public void loadNotifications() {
        api.getNotifications().enqueue(new Callback<List<NotificationDto>>() {
            @Override
            public void onResponse(Call<List<NotificationDto>> call, Response<List<NotificationDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> notifications = response.body().stream()
                            .map(NotificationDto::toModel)
                            .collect(Collectors.toList());
                    notificationsLiveData.postValue(notifications);
                    updateUnreadCount();
                } else {
                    Log.e(TAG, "Failed to load notifications: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<NotificationDto>> call, Throwable t) {
                Log.e(TAG, "Error loading notifications", t);
            }
        });
    }

    public void markAsRead(Integer notificationId) {
        api.markAsRead(notificationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    List<Notification> current = notificationsLiveData.getValue();
                    if (current != null) {
                        List<Notification> updated = current.stream()
                                .map(n -> {
                                    if (n.getId().equals(notificationId)) {
                                        n.setRead(true);
                                    }
                                    return n;
                                })
                                .collect(Collectors.toList());
                        notificationsLiveData.postValue(updated);
                        updateUnreadCount();
                    }
                } else {
                    Log.e(TAG, "Failed to mark as read: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error marking as read", t);
            }
        });
    }

    public void addNotification(Notification notification) {
        List<Notification> current = notificationsLiveData.getValue();
        if (current != null) {
            List<Notification> updated = new ArrayList<>();
            updated.add(notification);
            updated.addAll(current);
            notificationsLiveData.postValue(updated);
            updateUnreadCount();
        }
    }

    private void updateUnreadCount() {
        List<Notification> current = notificationsLiveData.getValue();
        if (current != null) {
            int count = (int) current.stream().filter(n -> !n.isRead()).count();
            unreadCountLiveData.postValue(count);
        }
    }

    public void clearAll() {
        notificationsLiveData.postValue(new ArrayList<>());
        unreadCountLiveData.postValue(0);
    }
}