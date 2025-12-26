package com.uberplus.backend.dto.route;

import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRouteCreateDTO {
    private String name;
    private LocationDTO startLocation;
    private LocationDTO endLocation;
    private List<LocationDTO> waypoints;
    private VehicleType vehicleType;
    private boolean babyFriendly;
    private boolean petsFriendly;
}
