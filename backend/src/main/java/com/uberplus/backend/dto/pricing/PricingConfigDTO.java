package com.uberplus.backend.dto.pricing;

import com.uberplus.backend.model.PricingConfig;
import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfigDTO {
    private Integer id;
    private VehicleType vehicleType;
    private Double basePrice;
    private Double pricePerKm;
    private LocalDateTime lastUpdated;
    private String updatedBy;

    public PricingConfigDTO(PricingConfig config) {
        this.id = config.getId();
        this.vehicleType = config.getVehicleType();
        this.basePrice = config.getBasePrice();
        this.pricePerKm = config.getPricePerKm();
        this.lastUpdated = config.getLastUpdated();
        this.updatedBy = config.getUpdatedBy();
    }
}