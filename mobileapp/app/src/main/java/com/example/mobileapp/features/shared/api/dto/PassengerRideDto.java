package com.example.mobileapp.features.shared.api.dto;

import java.util.List;

public class PassengerRideDto {
    public Integer id;
    public String status;
    public Integer vehicleId;
    public String vehicleModel;
    public String vehicleLicensePlate;
    public String driverEmail;
    public LocationDto startLocation;
    public LocationDto endLocation;
    public List<LocationDto> waypoints;
    public List<PassengerDto> passengers;
    public String scheduledTime;
}
