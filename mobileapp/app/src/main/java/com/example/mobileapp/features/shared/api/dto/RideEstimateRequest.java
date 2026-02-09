package com.example.mobileapp.features.shared.api.dto;

import com.example.mobileapp.features.shared.models.enums.VehicleType;

public class RideEstimateRequest {
    private int estimatedDistance;

    private VehicleType vehicleType;

    public RideEstimateRequest(int estimatedDistance, VehicleType vehicleType) {
        this.estimatedDistance = estimatedDistance;
        this.vehicleType = vehicleType;
    }

    public int getEstimatedDistance() {
        return estimatedDistance;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
}
