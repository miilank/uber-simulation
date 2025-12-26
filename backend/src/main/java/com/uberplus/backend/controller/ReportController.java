package com.uberplus.backend.controller;

import com.uberplus.backend.dto.report.RideHistoryFilterDTO;
import com.uberplus.backend.dto.report.RideHistoryResponseDTO;
import com.uberplus.backend.dto.report.RideReportDTO;
import com.uberplus.backend.repository.RideRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final RideRepository rideRepository;

    // GET /api/reports/my-rides
    @GetMapping("/my-rides")
    public ResponseEntity<RideHistoryResponseDTO> getMyRides(@Valid RideHistoryFilterDTO filter) {
        return ResponseEntity.ok(new RideHistoryResponseDTO());
    }

    //GET /api/reports/my-report
    @GetMapping("/my-report")
    public ResponseEntity<RideReportDTO> getMyReport(@RequestParam @Valid LocalDate startDate, @RequestParam @Valid LocalDate endDate) {
        return ResponseEntity.ok(new RideReportDTO());
    }
}
