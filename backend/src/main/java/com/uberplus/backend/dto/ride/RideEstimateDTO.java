package com.uberplus.backend.dto.ride;

import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideEstimateDTO {
    private int estimatedDistance;
    private VehicleType vehicleType;
}
