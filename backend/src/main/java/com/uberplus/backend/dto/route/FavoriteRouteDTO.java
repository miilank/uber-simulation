package com.uberplus.backend.dto.route;

import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.model.FavoriteRoute;
import com.uberplus.backend.model.Location;
import com.uberplus.backend.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRouteDTO {
    private Integer id;
    private String name;
    private LocationDTO startLocation;
    private LocationDTO endLocation;
    private List<LocationDTO> waypoints;
    private VehicleType vehicleType;
    private boolean babyFriendly;
    private boolean petsFriendly;
    private LocalDateTime createdAt;

    public FavoriteRouteDTO(FavoriteRoute r) {
        id = r.getId();
        name = r.getName();
        startLocation = new LocationDTO(r.getStartLocation());
        endLocation = new LocationDTO(r.getEndLocation());
        waypoints = r.getWaypoints().stream().map(LocationDTO::new).toList();
        vehicleType = r.getVehicleType();
        babyFriendly = r.isBabyFriendly();
        petsFriendly = r.isPetsFriendly();
        createdAt = r.getCreatedAt();
    }
}
