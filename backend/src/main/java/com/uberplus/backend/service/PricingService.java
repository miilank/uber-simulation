package com.uberplus.backend.service;

import com.uberplus.backend.dto.pricing.PricingConfigDTO;
import com.uberplus.backend.dto.pricing.PricingUpdateDTO;
import com.uberplus.backend.model.enums.VehicleType;

import java.util.List;

public interface PricingService {
    double calculatePrice(double distanceKm, VehicleType vehicleType);

    List<PricingConfigDTO> getAllPricing();

    PricingConfigDTO updatePricing(VehicleType vehicleType, PricingUpdateDTO request, String adminEmail);
}