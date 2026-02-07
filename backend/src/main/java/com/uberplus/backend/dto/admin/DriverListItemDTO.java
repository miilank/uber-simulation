package com.uberplus.backend.dto.admin;

import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.Ride;
import com.uberplus.backend.model.enums.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverListItemDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean available;
    private Boolean active;
    private Boolean hasActiveRide;
    private Integer currentRideId;

    public DriverListItemDTO(Driver driver) {
        this.id = driver.getId();
        this.firstName = driver.getFirstName();
        this.lastName = driver.getLastName();
        this.email = driver.getEmail();
        this.available = driver.isAvailable();
        this.active = driver.isActive();

        this.hasActiveRide = driver.getRides().stream()
                .anyMatch(ride -> ride.getStatus() == RideStatus.ACCEPTED ||
                        ride.getStatus() == RideStatus.IN_PROGRESS);

        this.currentRideId = driver.getRides().stream()
                .filter(ride -> ride.getStatus() == RideStatus.ACCEPTED ||
                        ride.getStatus() == RideStatus.IN_PROGRESS)
                .map(Ride::getId)
                .findFirst()
                .orElse(null);
    }
}