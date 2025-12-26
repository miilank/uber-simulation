package com.uberplus.backend.dto.ride;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideStopEarlyDTO {
    private Integer rideId;
    private LocationDTO stopLocation;
}
