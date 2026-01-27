package com.uberplus.backend.dto.ride;

import com.uberplus.backend.model.enums.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRideRequestDTO {
    @NotNull(message = "Start location is required")
    @Valid
    private LocationDTO startLocation;

    @NotNull(message = "End location is required")
    @Valid

    private LocationDTO endLocation;
    @Valid

    private List<LocationDTO> waypoints;

    private VehicleType vehicleType;
    private boolean babyFriendly;
    private boolean petFriendly;
    private List<String> linkedPassengerEmails;

    @NotNull(message="Scheduled time is required.")
    private LocalDateTime scheduledTime;

    @NotNull(message="Estimated duration is required.")
    private int estimatedDurationMinutes;

    @NotNull(message="Distance is required.")
    private Double distanceKm;
}
