package com.uberplus.backend.dto.ride;

import com.uberplus.backend.model.Location;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Address is required")
    private String address;

    public LocationDTO(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        address = location.getAddress();
    }

    public Location toEntity() {
        Location location = new Location();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAddress(address);
        location.setCreatedAt(LocalDateTime.now());
        return location;
    }
}
