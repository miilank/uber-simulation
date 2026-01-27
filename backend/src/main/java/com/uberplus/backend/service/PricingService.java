package com.uberplus.backend.service;

import com.uberplus.backend.dto.ride.RideEstimateDTO;
import com.uberplus.backend.model.enums.VehicleType;

public interface PricingService {
    double calculatePrice(double distanceKm, VehicleType vehicleType);
}
