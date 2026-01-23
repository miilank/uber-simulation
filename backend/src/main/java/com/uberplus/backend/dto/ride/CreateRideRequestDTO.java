package com.uberplus.backend.dto.ride;

import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRideRequestDTO {
    private LocationDTO startLocation;
    private LocationDTO endLocation;
    private List<LocationDTO> waypoints;
    private VehicleType vehicleType;
    private boolean babyFriendly;
    private boolean petFriendly;
    private List<String> linkedPassengerEmails;
    private LocalDateTime scheduledTime;
    private Long favoriteRouteId;

    private int estimatedDurationMinutes;
}
