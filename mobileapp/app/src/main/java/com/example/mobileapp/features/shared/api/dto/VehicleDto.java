package com.example.mobileapp.features.shared.api.dto;

import com.example.mobileapp.features.shared.models.enums.VehicleStatus;
import com.example.mobileapp.features.shared.models.enums.VehicleType;

public class VehicleDto {
    private Integer id;
    private String model;
    private VehicleType type;
    private String licensePlate;
    private int seatCount;
    private boolean babyFriendly;
    private boolean petsFriendly;
    private VehicleStatus status;


    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public boolean isPetsFriendly() {
        return petsFriendly;
    }

    public void setPetsFriendly(boolean petsFriendly) {
        this.petsFriendly = petsFriendly;
    }

    public boolean isBabyFriendly() {
        return babyFriendly;
    }

    public void setBabyFriendly(boolean babyFriendly) {
        this.babyFriendly = babyFriendly;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
