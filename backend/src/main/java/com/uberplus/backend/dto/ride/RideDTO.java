package com.uberplus.backend.dto.ride;

import com.uberplus.backend.dto.passenger.PassengerDTO;
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
    private String creatorEmail;
    private String driverEmail;
    private RideStatus status;
    private LocationDTO startLocation;
    private LocationDTO endLocation;
    private List<LocationDTO> waypoints;
    private List<PassengerDTO> passengers;
    private List<String> passengerEmails;
    private VehicleType vehicleType;
    private double totalPrice;
    private boolean panicActivated;
    private String cancelledBy;
    private LocalDateTime scheduledTime;
    private LocalDateTime estimatedStartTime;

    private boolean babyFriendly;
    private boolean petsFriendly;

    private Integer vehicleId;
    private String vehicleModel;
    private String vehicleLicensePlate;

    public RideDTO(Ride ride) {
        this.id = ride.getId();
//        this.creatorId = ride.getCreator().getId();
        this.creatorEmail = ride.getCreator().getEmail();

//        this.driverId = ride.getDriver().getId();
        this.driverEmail = ride.getDriver().getEmail();
        this.status = ride.getStatus();

        this.startLocation = new LocationDTO(ride.getStartLocation());
        this.endLocation = new LocationDTO(ride.getEndLocation());

        this.waypoints = ride.getWaypoints()
                .stream()
                .map(LocationDTO::new)
                .toList();
        this.passengers = ride.getPassengers()
                .stream()
                .map(PassengerDTO::new)
                .toList();
        this.passengerEmails = ride.getPassengers()
                .stream()
                .map(Passenger::getEmail)
                .toList();

        this.vehicleType = ride.getVehicleType();
        this.totalPrice = ride.getTotalPrice() != null ? ride.getTotalPrice() : 0.0;
        this.panicActivated = ride.isPanicActivated();
        this.cancelledBy = ride.getCancelledBy();
        this.scheduledTime = ride.getScheduledTime();

        this.petsFriendly = ride.isPetsFriendly();
        this.babyFriendly = ride.isBabyFriendly();

        if (ride.getDriver() != null && ride.getDriver().getVehicle() != null) {
            this.vehicleId = ride.getDriver().getVehicle().getId();
            this.vehicleModel = ride.getDriver().getVehicle().getModel();
            this.vehicleLicensePlate = ride.getDriver().getVehicle().getLicensePlate();
        }

        this.estimatedStartTime = ride.getEstimatedStartTime();
    }
}
