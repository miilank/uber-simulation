package com.uberplus.backend.dto.vehicle;

import com.uberplus.backend.model.Vehicle;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private Integer id;
    private String model;
    private VehicleType type;
    private String licensePlate;
    private int seatCount;
    private boolean babyFriendly;
    private boolean petsFriendly;
    private VehicleStatus status;

    public VehicleDTO(Vehicle vehicle) {
        this.id = vehicle.getId();
        this.model = vehicle.getModel();
        this.type = vehicle.getType();
        this.licensePlate = vehicle.getLicensePlate();
        this.seatCount = vehicle.getSeatCount();
        this.babyFriendly = vehicle.isBabyFriendly();
        this.petsFriendly = vehicle.isPetsFriendly();
        this.status = vehicle.getStatus();
    }
}
