package com.uberplus.backend.dto.pricing;

import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfigDTO {
    private VehicleType vehicleType;
    private double basePrice;
    private double pricePerKm;
}