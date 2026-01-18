package com.uberplus.backend.controller;

import com.uberplus.backend.dto.driver.DriverCreationDTO;
import com.uberplus.backend.dto.driver.DriverProfileDTO;
import com.uberplus.backend.dto.driver.DriverStatusUpdateDTO;
import com.uberplus.backend.dto.user.UserUpdateDTO;
import com.uberplus.backend.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    // POST /api/drivers
    @PostMapping
    public ResponseEntity<DriverProfileDTO> createDriver(@Valid @RequestBody DriverCreationDTO request) {
        return ResponseEntity.ok(new DriverProfileDTO());
    }

    // GET /api/drivers/profile
    @GetMapping("/profile")
    public ResponseEntity<DriverProfileDTO> getProfile(Authentication authentication) {
        return ResponseEntity.ok(driverService.getProfile(authentication.getName()));
    }

    // PUT /api/drivers/profile
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(Authentication authentication, @Valid @RequestBody UserUpdateDTO update) {
        driverService.requestProfileUpdate(authentication.getName(), update);
        return ResponseEntity.accepted().build();
    }

    // PUT /api/drivers/{driverId}/approve-update
    @PutMapping("/{driverId}/approve-update")
    public ResponseEntity<Void> approveUpdate(@PathVariable Integer driverId) {
        driverService.approveProfileUpdate(driverId);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/drivers/{driverId}/reject-update
    public ResponseEntity<Void> rejectUpdate(@PathVariable Integer driverId) {
        driverService.rejectProfileUpdate(driverId);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/drivers/{driverId}/status
    @PutMapping("/{driverId}/status")
    public ResponseEntity<DriverProfileDTO> updateStatus(@PathVariable Integer driverId, @Valid @RequestBody DriverStatusUpdateDTO request) {
        return ResponseEntity.ok(new DriverProfileDTO());
    }
}