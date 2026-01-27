package com.uberplus.backend.dto.ride;

import com.uberplus.backend.model.Location;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Latitude is required")
    private Double latitude;

    @NotBlank(message = "Longitude is required")
    private Double longitude;

    @NotBlank(message = "Address is required")
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
