package com.example.mobileapp.features.shared.api.dto;

public class RideCancellationDto {
    private int userId;
    private String reason;

    public RideCancellationDto() {
    }

    public RideCancellationDto(int userId, String reason) {
        this.userId = userId;
        this.reason = reason;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
