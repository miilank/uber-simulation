package com.uberplus.backend.service;

import com.uberplus.backend.dto.report.RideHistoryFilterDTO;
import com.uberplus.backend.dto.report.RideHistoryResponseDTO;
import com.uberplus.backend.dto.ride.RideDetailDTO;

public interface RideHistoryService {
    RideHistoryResponseDTO getDriverHistory(Integer driverId, RideHistoryFilterDTO filter);
    RideDetailDTO getRideDetails(Integer rideId);
}
