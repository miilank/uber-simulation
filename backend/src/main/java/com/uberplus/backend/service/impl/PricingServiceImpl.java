package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.ride.RideEstimateDTO;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.service.PricingService;
import org.springframework.stereotype.Service;

@Service
public class PricingServiceImpl implements PricingService {

    public double calculatePrice(double distanceKm, VehicleType vehicleType) {

        double basePrice = switch (vehicleType) {
            case STANDARD -> 2.5;
            case LUXURY -> 4.5;
            case VAN -> 5.5;
        };

        double total = basePrice + (distanceKm * 1.20);

        return Math.max(4.0, Math.ceil(total * 2) / 2.0);
    }
}
