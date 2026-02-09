package com.example.mobileapp.features.shared.api.dto;

public class RatingRequestDto {
    public Integer rideId;
    public Integer vehicleRating;
    public Integer driverRating;
    public String comment;

    public RatingRequestDto(Integer rideId, Integer vehicleRating, Integer driverRating, String comment) {
        this.rideId = rideId;
        this.vehicleRating = vehicleRating;
        this.driverRating = driverRating;
        this.comment = comment;
    }
}
