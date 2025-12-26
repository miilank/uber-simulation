package com.uberplus.backend.controller;

import com.uberplus.backend.dto.driver.DriverCreationDTO;
import com.uberplus.backend.dto.driver.DriverProfileDTO;
import com.uberplus.backend.dto.driver.DriverStatusUpdateDTO;
import com.uberplus.backend.repository.DriverRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverRepository driverRepository;

    // POST /api/drivers
    @PostMapping
    public ResponseEntity<DriverProfileDTO> createDriver(@Valid @RequestBody DriverCreationDTO request) {
        return ResponseEntity.ok(new DriverProfileDTO());
    }

    // GET /api/drivers/{driverId}/profile
    @GetMapping("/{driverId}/profile")
    public ResponseEntity<DriverProfileDTO> getProfile(@PathVariable Long driverId) {
        return ResponseEntity.ok(new DriverProfileDTO());
    }

    // PUT /api/drivers/{driverId}/status
    @PutMapping("/{driverId}/status")
    public ResponseEntity<DriverProfileDTO> updateStatus(@PathVariable Integer driverId, @Valid @RequestBody DriverStatusUpdateDTO request) {
        return ResponseEntity.ok(new DriverProfileDTO());
    }
}