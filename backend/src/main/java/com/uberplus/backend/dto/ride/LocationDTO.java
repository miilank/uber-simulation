package com.uberplus.backend.dto.ride;

import com.uberplus.backend.model.Location;
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
    private Double latitude;
    private Double longitude;
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
