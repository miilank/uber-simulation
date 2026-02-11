package com.uberplus.backend.service;

import com.uberplus.backend.dto.report.RideHistoryFilterDTO;
import com.uberplus.backend.dto.report.RideHistoryResponseDTO;
import com.uberplus.backend.dto.ride.HistoryReportDTO;
import com.uberplus.backend.dto.ride.RideDetailDTO;

import java.time.LocalDate;

public interface RideHistoryService {
    RideHistoryResponseDTO getDriverHistory(Integer driverId, RideHistoryFilterDTO filter);
    RideDetailDTO getRideDetails(Integer rideId);
    RideHistoryResponseDTO getPassengerHistory(Integer userId, RideHistoryFilterDTO filter);
    HistoryReportDTO getRideHistoryReport(String name, LocalDate from, LocalDate to, Integer uuid);
}
