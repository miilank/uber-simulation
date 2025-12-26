package com.uberplus.backend.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDTO {
    private Integer id;
    private Integer rideId;
    private String driverName;
    private int vehicleRating;
    private int driverRating;
    private String comment;
    private LocalDateTime createdAt;
}
