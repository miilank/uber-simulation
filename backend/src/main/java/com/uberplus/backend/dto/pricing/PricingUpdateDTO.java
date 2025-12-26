package com.uberplus.backend.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingUpdateDTO {
    private double basePrice;
    private double pricePerKm;
}
