package com.uberplus.backend.service;

import com.uberplus.backend.dto.report.RideHistoryFilterDTO;
import com.uberplus.backend.dto.report.RideHistoryResponseDTO;

public interface RideHistoryService {
    RideHistoryResponseDTO getDriverHistory(Integer driverId, RideHistoryFilterDTO filter);
}
