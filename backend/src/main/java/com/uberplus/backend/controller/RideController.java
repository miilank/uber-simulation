package com.uberplus.backend.controller;

import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.pricing.PriceEstimateResponseDTO;
import com.uberplus.backend.dto.ride.*;
import com.uberplus.backend.service.PricingService;
import com.uberplus.backend.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.uberplus.backend.dto.report.RideHistoryFilterDTO;
import com.uberplus.backend.dto.report.RideHistoryResponseDTO;
import com.uberplus.backend.service.RideHistoryService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;
    private final RideHistoryService rideHistoryService;
    private final PricingService pricingService;

    // POST /api/rides/estimate
    @PostMapping("/estimate")
    public ResponseEntity<PriceEstimateResponseDTO> estimateRide(@Valid @RequestBody RideEstimateDTO request) {
        double price = pricingService.calculatePrice(request.getEstimatedDistance()/1000.0,
                                                                request.getVehicleType());

        return ResponseEntity.ok(new PriceEstimateResponseDTO(
                price,
                String.format("€%.2f",price)
        ));
    }
    
    // POST /api/rides
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RideDTO> createRide(Authentication auth, @Valid @RequestBody CreateRideRequestDTO request) {
        return ResponseEntity.ok(rideService.requestRide(auth.getName(), request));
    }

    // Za sad samo driver. Ne znam da li treba za druge.
    // GET /api/rides
    @GetMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<RideDTO>> getRides(Authentication auth) {
        return ResponseEntity.ok(rideService.getRides(auth.getName()));
    }

    // GET /api/rides/passenger
    @GetMapping("/passenger")
    public ResponseEntity<List<RideDTO>> getPassengerRides(Authentication auth) {
        return ResponseEntity.ok(rideService.getPassengerRides(auth.getName()));
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
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideHistoryResponseDTO> getRideHistory(
            @RequestParam Integer driverId,
            @Valid RideHistoryFilterDTO filter
    ) {
        return ResponseEntity.ok(rideHistoryService.getDriverHistory(driverId, filter));
    }

    // POST /api/rides/{rideId}/cancel
    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<RideDTO> cancelRide(@PathVariable Integer rideId, @RequestBody RideCancellationDTO request) {
        RideDTO response = rideService.cancelRide(rideId,request.getReason(), request.getUserId());
        return ResponseEntity.ok(response);
    }

    // PUT /api/rides/{rideId}/start
    @PutMapping("/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideDTO> startRide(@PathVariable Integer rideId) {
        return ResponseEntity.ok(rideService.startRide(rideId));
    }

    // PUT /api/rides/{rideId}/complete
    @PutMapping("/{rideId}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideDTO> completeRide(
            Authentication auth,
            @PathVariable Integer rideId
    ) {
        RideDTO completed = rideService.completeRide(rideId, auth.getName());
        return ResponseEntity.ok(completed);
    }

    // POST /api/rides/{rideId}/panic
    @PostMapping("/{rideId}/panic")
    public ResponseEntity<Void> panic(@PathVariable Integer rideId, @RequestBody RidePanicDTO panicDto) {
        rideService.setPanic(rideId, panicDto.getUserId());
        return ResponseEntity.ok().build();
    }

    // POST /api/rides/{rideId}/stop-early
    @PostMapping("/{rideId}/stop-early")
    public ResponseEntity<RideDTO> stopEarly(@PathVariable Integer rideId, @RequestBody LocationDTO request) {
        RideDTO stopped = rideService.stopEarly(rideId,request);
        return ResponseEntity.ok(stopped);
    }

    // GET /api/rides/current-in-progress
    @GetMapping("/current-in-progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RideDTO> getMyInProgress(Authentication auth) {
        return ResponseEntity.ok(rideService.getInProgressForPassenger(auth.getName()));
    }

    @GetMapping("/{id}/eta")
    public ResponseEntity<RideETADTO> getRideETA(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(rideService.getRideETA(id));
        } catch (IOException | InterruptedException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not calculate ETA");
        }
    }

    @PutMapping("/{id}/arrived-pickup")
    public ResponseEntity<RideDTO> arrivedAtPickup(@PathVariable Integer id) {
        return ResponseEntity.ok(rideService.arrivedAtPickup(id));
    }

    // POST /api/rides/{rideId}/inconsistency
    @PostMapping("/{rideId}/inconsistency")
    public ResponseEntity<MessageDTO> reportInconsistency(
            @PathVariable Integer rideId,
            @Valid @RequestBody RideInconsistencyDTO request) {
        rideService.reportInconsistency(rideId, request.getPassengerId(), request.getDescription());
        return ResponseEntity.ok(new MessageDTO());
    }
}
