package com.example.mobileapp.features.shared.api.dto;

import com.example.mobileapp.features.shared.models.Notification;
import com.example.mobileapp.features.shared.models.enums.NotificationType;

import java.time.LocalDateTime;

public class NotificationDto {
    private Integer id;
    private NotificationType type;
    private String message;
    private Integer rideId;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRideId() {
        return rideId;
    }

    public void setRideId(Integer rideId) {
        this.rideId = rideId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Notification toModel() {
        return new Notification(id, type, message, rideId, read, createdAt);
    }
}