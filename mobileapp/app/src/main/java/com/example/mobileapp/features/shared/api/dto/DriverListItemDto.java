package com.example.mobileapp.features.shared.api.dto;

public class DriverListItemDto {
    public Integer id;
    public String firstName;
    public String lastName;
    public String email;
    public Boolean available;
    public Boolean active;
    public Boolean hasActiveRide;
    public Integer currentRideId;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}