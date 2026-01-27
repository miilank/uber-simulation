package com.uberplus.backend.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequestDTO {
    @NotNull
    private Integer rideId;

    @Min(1) @Max(5)
    private int vehicleRating;

    @Min(1) @Max(5)
    private int driverRating;

    private String comment;
}
