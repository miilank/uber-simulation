package com.uberplus.backend.dto.driver;

import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.vehicle.VehicleDTO;
import com.uberplus.backend.model.Driver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class DriverDTO extends UserProfileDTO {
    private boolean available;
    private boolean active;
    private double workedMinutesLast24h;
    private Double averageRating;
    private VehicleDTO vehicle;

    public DriverDTO(Driver driver) {
        this.id = driver.getId();
        this.email = driver.getEmail();
        this.firstName = driver.getFirstName();
        this.lastName = driver.getLastName();
        this.phoneNumber = driver.getPhoneNumber();
        this.address = driver.getAddress();
        this.profilePicture = driver.getProfilePicture();
        this.role = driver.getRole();
        this.blocked = driver.isBlocked();
        this.blockReason = driver.getBlockReason();
        this.activated = driver.isActivated();
        this.available = driver.isAvailable();
        this.active = driver.isActive();
        this.workedMinutesLast24h = driver.getWorkedMinutesLast24h();
        this. averageRating = driver.getAverageRating();
        this.vehicle = new VehicleDTO(driver.getVehicle());
    }
}
