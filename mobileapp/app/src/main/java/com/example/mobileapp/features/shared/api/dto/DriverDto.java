package com.example.mobileapp.features.shared.api.dto;

import com.example.mobileapp.features.shared.models.User;


public class DriverDto extends User {
    private boolean available;
    private boolean active;
    private double workedMinutesLast24h;
    private Double averageRating;
    private VehicleDto vehicle;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getWorkedMinutesLast24h() {
        return workedMinutesLast24h;
    }

    public void setWorkedMinutesLast24h(double workedMinutesLast24h) {
        this.workedMinutesLast24h = workedMinutesLast24h;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public VehicleDto getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleDto vehicle) {
        this.vehicle = vehicle;
    }

}
