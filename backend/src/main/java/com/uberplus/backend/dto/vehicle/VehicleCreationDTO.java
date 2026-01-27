package com.uberplus.backend.dto.vehicle;

import com.uberplus.backend.model.enums.VehicleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCreationDTO {
    @NotBlank(message = "Vehicle model is required")
    private String model;

    @NotNull(message = "Vehicle type is required")
    private VehicleType type;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @Min(value = 1, message = "seatCount must be >= 1")
    @Max(value = 12, message = "seatCount seems too large")
    private int seatCount;

    private boolean babyFriendly;
    private boolean petsFriendly;
}
