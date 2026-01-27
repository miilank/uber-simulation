package com.uberplus.backend.dto.ride;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideInconsistencyDTO {
    private Integer rideId;
    private Integer passengerId;
    private String passengerName;
    private String description;
    private LocalDateTime createdAt;
}
