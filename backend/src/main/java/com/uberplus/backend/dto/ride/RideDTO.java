package com.uberplus.backend.dto.ride;

import com.uberplus.backend.model.Passenger;
import com.uberplus.backend.model.Ride;
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
    private String creatorEmail;
    private Integer driverId;
    private String driverEmail;
    private RideStatus status;
    private LocationDTO startLocation;
    private LocationDTO endLocation;
    private List<LocationDTO> waypoints;
    private List<Integer> passengerIds;
    private VehicleType vehicleType;
    private double totalPrice;
    private LocalDateTime createdAt;

    public RideDTO(Ride ride) {
        this.id = ride.getId();

        this.creatorId = ride.getCreator().getId();
        this.creatorEmail = ride.getCreator().getEmail();


        this.driverId = ride.getDriver().getId();
        this.driverEmail = ride.getDriver().getEmail();


        this.status = ride.getStatus();

        this.startLocation = new LocationDTO(ride.getStartLocation());
        this.endLocation = new LocationDTO(ride.getEndLocation());

        this.waypoints = ride.getWaypoints()
                .stream()
                .map(LocationDTO::new)
                .toList();

        this.passengerIds = ride.getPassengers()
                .stream()
                .map(Passenger::getId)
                .toList();

        this.vehicleType = ride.getVehicleType();
        this.totalPrice = ride.getTotalPrice() != null ? ride.getTotalPrice() : 0.0;
        this.createdAt = ride.getCreatedAt();
    }
}
