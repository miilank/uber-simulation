package com.uberplus.backend.dto.route;

import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.model.enums.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRouteCreateDTO {
    private String name;

    @NotNull(message = "End location is required")
    @Valid
    private LocationDTO startLocation;

    @NotNull(message = "Start location is required")
    @Valid
    private LocationDTO endLocation;

    @Valid
    private List<LocationDTO> waypoints;

    private VehicleType vehicleType;

    private boolean babyFriendly;
    private boolean petsFriendly;
}
