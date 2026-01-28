package com.uberplus.backend.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceEstimateResponseDTO {
    private double finalPrice;
    private String priceDisplay;
}