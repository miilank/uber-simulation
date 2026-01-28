package com.uberplus.backend.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideInconsistencyDTO {
    private Integer rideId; // validacija dolazi iz path
    @NotNull(message = "Passenger ID is required")
    private Integer passengerId;
    private String passengerName; // output only
    @NotBlank(message = "Description is required")
    @Size(max = 300, message = "Description cannot exceed 300 characters")
    private String description;
    private LocalDateTime createdAt; // output only
}
