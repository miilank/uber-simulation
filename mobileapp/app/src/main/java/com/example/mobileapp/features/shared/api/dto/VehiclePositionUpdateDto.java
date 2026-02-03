package com.example.mobileapp.features.shared.api.dto;

public class VehiclePositionUpdateDto {
    public double latitude;
    public double longitude;

    public VehiclePositionUpdateDto(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
