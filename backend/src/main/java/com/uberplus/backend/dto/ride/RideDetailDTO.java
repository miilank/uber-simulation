package com.uberplus.backend.dto.ride;

import com.uberplus.backend.dto.driver.DriverDTO;
import com.uberplus.backend.dto.passenger.PassengerDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideDetailDTO {
    private Integer id;
    private String status;

    private String startAddress;
    private String endAddress;

    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime estimatedStartTime;
    private LocalDateTime estimatedEndTime;
    private List<LocationDTO> waypoints;
    private List<PassengerDTO> passengers;
    private DriverDTO driver;

    private Double totalPrice;

    private String cancelledBy;
    private String cancellationReason;
    private LocalDateTime cancellationTime;

    private boolean panicActivated;
    private String panicActivatedBy;
    private LocalDateTime panicActivatedAt;

    private String stoppedLocation;
    private LocalDateTime stoppedAt;

    private List<RideInconsistencyDTO> inconsistencies;
}