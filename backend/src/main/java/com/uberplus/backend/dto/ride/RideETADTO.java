package com.uberplus.backend.dto.ride;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideETADTO {
    private Integer rideId;
    private Double distanceToNextPointKm;
    private Integer etaToNextPointSeconds;
    private String phase; // "TO_PICKUP" ili "IN_PROGRESS"
    private Double progressPercent;
}