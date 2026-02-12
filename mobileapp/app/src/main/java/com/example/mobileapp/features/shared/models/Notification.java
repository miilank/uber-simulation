package com.example.mobileapp.features.shared.models;

import com.example.mobileapp.features.shared.models.enums.NotificationType;

import java.time.LocalDateTime;

public class Notification {
    private Integer id;
    private NotificationType type;
    private String message;
    private Integer rideId;
    private boolean read;
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(Integer id, NotificationType type, String message, Integer rideId,
                        boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.rideId = rideId;
        this.read = read;
        this.createdAt = createdAt;
    }

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
}