package com.uberplus.backend.controller;

import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.ride.*;
import com.uberplus.backend.repository.RideRepository;
import com.uberplus.backend.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.uberplus.backend.dto.report.RideHistoryFilterDTO;
import com.uberplus.backend.dto.report.RideHistoryResponseDTO;
import com.uberplus.backend.service.RideHistoryService;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;
    private final RideHistoryService rideHistoryService;

    // POST /api/rides/estimate
    @PostMapping("/estimate")
    public ResponseEntity<RideEstimateDTO> estimateRide(@Valid @RequestBody CreateRideRequestDTO request) {
        return ResponseEntity.ok(new RideEstimateDTO());
    }
    // POST /api/rides
    @PostMapping
    public ResponseEntity<RideDTO> createRide(Authentication auth, @Valid @RequestBody CreateRideRequestDTO request) {
        return ResponseEntity.ok(rideService.reqestRide(auth.getName(), request));
    }

    // GET /api/rides/{rideId}
    @GetMapping("/{rideId}")
    public ResponseEntity<RideDTO> getRide(@PathVariable Integer rideId) {
        return ResponseEntity.ok(new RideDTO());
    }

    // GET /api/rides/active
    @GetMapping("/active")
    public ResponseEntity<List<RideDTO>> getActiveRides() {
        return ResponseEntity.ok(List.of(new RideDTO()));
    }

    // GET /api/rides/history?driverId=1&startDate=2026-01-01&endDate=2026-01-31&page=0&size=20
    @GetMapping("/history")
    public ResponseEntity<RideHistoryResponseDTO> getRideHistory(
            @RequestParam Integer driverId,
            @Valid RideHistoryFilterDTO filter
    ) {
        return ResponseEntity.ok(rideHistoryService.getDriverHistory(driverId, filter));
    }

    // POST /api/rides/{rideId}/cancel
    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<RideDTO> cancelRide(@PathVariable Integer rideId, @RequestBody RideCancellationDTO request) {
        return ResponseEntity.ok(new RideDTO());
    }

    // PUT /api/rides/{rideId}/start
    @PutMapping("/{rideId}/start")
    public ResponseEntity<RideDTO> startRide(@PathVariable Integer rideId) {
        return ResponseEntity.ok(new RideDTO());
    }

    // PUT /api/rides/{rideId}/end
    @PutMapping("/{rideId}/end")
    public ResponseEntity<RideDTO> endRide(@PathVariable Integer rideId) {
        return ResponseEntity.ok(new RideDTO());
    }

    // POST /api/rides/{rideId}/panic
    @PostMapping("/{rideId}/panic")
    public ResponseEntity<MessageDTO> panic(@PathVariable Integer rideId,  @Valid @RequestBody RidePanicDTO request) {
        return ResponseEntity.ok(new MessageDTO());
    }

    // POST /api/rides/{rideId}/stop-early
    @PostMapping("/{rideId}/stop-early")
    public ResponseEntity<RideDTO> stopEarly(@PathVariable Integer rideId, @Valid @RequestBody RideStopEarlyDTO request) {
        return ResponseEntity.ok(new RideDTO());
    }

    // POST /api/rides/{rideId}/inconsistency
    @PostMapping("/{rideId}/inconsistency")
    public ResponseEntity<MessageDTO> reportInconsistency(@PathVariable Integer rideId, @RequestBody RideInconsistencyDTO request) {
        return ResponseEntity.ok(new MessageDTO());
    }
}
