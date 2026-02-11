package com.example.mobileapp.features.shared.api.dto;

import java.util.List;

public class RideDetailDto {
    public Integer id;
    public String status;

    public String startAddress;
    public String endAddress;

    public String actualStartTime;
    public String actualEndTime;
    public String estimatedStartTime;
    public String estimatedEndTime;

    public List<PassengerDto> passengers;

    public DriverDto driver;

    public Double totalPrice;

    public String cancelledBy;
    public String cancellationReason;
    public String cancellationTime;

    public Boolean panicActivated;
    public String panicActivatedBy;
    public String panicActivatedAt;

    public String stoppedLocation;
    public String stoppedAt;

    public List<RideInconsistencyDto> inconsistencies;

    public List<LocationDto> waypoints;
}
