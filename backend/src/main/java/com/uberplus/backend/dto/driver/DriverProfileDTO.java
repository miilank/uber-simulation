package com.uberplus.backend.dto.driver;

import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.vehicle.VehicleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class DriverProfileDTO extends UserProfileDTO {
    private boolean available;
    private boolean active;
    private double workedMinutesLast24h;
    private Double averageRating;
    private VehicleDTO vehicle;
}
