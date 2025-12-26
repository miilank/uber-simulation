package com.uberplus.backend.dto.vehicle;

import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCreationDTO {
    private String model;
    private VehicleType type;
    private String licensePlate;
    private int seatCount;
    private boolean babyFriendly;
    private boolean petsFriendly;
}
