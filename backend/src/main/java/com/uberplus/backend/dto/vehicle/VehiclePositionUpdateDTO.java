package com.uberplus.backend.dto.vehicle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePositionUpdateDTO {
    @Min(value = -90, message = "Latitude must be >= -90")
    @Max(value = 90, message = "Latitude must be <= 90")
    private double latitude;
    @Min(value = -180, message = "Longitude must be >= -180")
    @Max(value = 180, message = "Longitude must be <= 180")
    private double longitude;
}