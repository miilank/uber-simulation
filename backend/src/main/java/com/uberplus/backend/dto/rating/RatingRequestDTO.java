package com.uberplus.backend.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequestDTO {
    private Integer rideId;
    private int vehicleRating;
    private int driverRating;
    private String comment;
}
