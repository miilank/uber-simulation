package com.uberplus.backend.dto.ride;

import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideEstimateDTO {
    private LocationDTO startLocation;
    private LocationDTO endLocation;
    private VehicleType vehicleType;
    private int estimatedMinutes;
    private double estimatedPrice;
}
