package com.uberplus.backend.dto.pricing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingUpdateDTO {

    @NotNull(message = "Base price is required")
    @Min(value = 0, message = "Base price must be positive")
    private Double basePrice;

    @NotNull(message = "Price per km is required")
    @Min(value = 0, message = "Price per km must be positive")
    private Double pricePerKm;
}