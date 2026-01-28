package com.uberplus.backend.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequestDTO {
    @NotNull(message = "Ride ID is required")
    private Integer rideId;

    @Min(value = 1, message = "Vehicle rating must be at least 1")
    @Max(value = 5, message = "Vehicle rating must be at most 5")
    private int vehicleRating;

    @Min(value = 1, message = "Driver rating must be at least 1")
    @Max(value = 5, message = "Driver rating must be at most 5")
    private int driverRating;

    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;
}
