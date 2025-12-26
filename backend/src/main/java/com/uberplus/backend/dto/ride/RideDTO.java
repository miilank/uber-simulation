package com.uberplus.backend.dto.ride;

import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideDTO {
    private Integer id;
    private Integer creatorId;
    private String creatorName;
    private Integer driverId;
    private String driverName;
    private RideStatus status;
    private LocationDTO startLocation;
    private LocationDTO endLocation;
    private List<LocationDTO> waypoints;
    private List<Integer> passengerIds;
    private VehicleType vehicleType;
    private double totalPrice;
    private LocalDateTime createdAt;
}
