package com.example.mobileapp.features.shared.api.dto;

import java.util.List;

public class DriverRideDto {
    public Integer id;
    public String creatorEmail;
    public String driverEmail;
    public String status;

    public LocationDto startLocation;
    public LocationDto endLocation;
    public List<LocationDto> waypoints;

    public List<PassengerDto> passengers;
    public List<String> passengerEmails;

    public String vehicleType;
    public double basePrice;

    public boolean panicActivated;
    public String cancelledBy;

    public String scheduledTime;
    public String estimatedStartTime;

    public boolean babyFriendly;
    public boolean petsFriendly;

    public Integer vehicleId;
    public String vehicleModel;
    public String vehicleLicensePlate;

    public String arrivedAtPickupTime;
}
