package com.uberplus.backend.controller;

import com.uberplus.backend.dto.driver.*;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import com.uberplus.backend.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    // POST /api/drivers
    @PostMapping
    public ResponseEntity<Void> createDriver(@Valid @RequestBody DriverCreationDTO request) {
        Integer id = driverService.createDriver(request);
        URI location = ServletUriComponentsBuilder
                .fromPath("/api/drivers/" + id.toString())
                .build()
                .toUri();
        return ResponseEntity.created(location).build();
    }

    // PUT /api/drivers/activate
    @PutMapping("/activate")
    public ResponseEntity<Void> activateDriver(@Valid @RequestBody DriverActivationDTO dto) {
        driverService.activateDriver(dto);
        return ResponseEntity.noContent().build();
    }

    // GET /api/drivers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DriverDTO> getDriver(@PathVariable Integer driverId) {;
        return ResponseEntity.ok(driverService.getDriver(driverId));
    }

    // GET /api/drivers/profile
    @GetMapping("/profile")
    public ResponseEntity<DriverDTO> getProfile(Authentication authentication) {
        return ResponseEntity.ok(driverService.getProfile(authentication.getName()));
    }

    // PUT /api/drivers/profile
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(Authentication authentication, @Valid @RequestBody UserUpdateRequestDTO update) {
        driverService.requestProfileUpdate(authentication.getName(), update);
        return ResponseEntity.accepted().build();
    }

    // GET /api/drivers/pending-updates
    @GetMapping("/pending-updates")
    public ResponseEntity<List<DriverUpdateDTO>> getPendingUpdates() {
        return ResponseEntity.ok(driverService.getPendingUpdates());
    }

    // PUT /api/drivers/{driverId}/approve-update
    @PutMapping("/{driverId}/approve-update")
    public ResponseEntity<Void> approveUpdate(@PathVariable Integer driverId) {
        driverService.approveProfileUpdate(driverId);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/drivers/{driverId}/reject-update
    @PutMapping("/{driverId}/reject-update")
    public ResponseEntity<Void> rejectUpdate(@PathVariable Integer driverId) {
        driverService.rejectProfileUpdate(driverId);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/drivers/{driverId}/status
    @PutMapping("/{driverId}/status")
    public ResponseEntity<DriverDTO> updateStatus(@PathVariable Integer driverId, @Valid @RequestBody DriverStatusUpdateDTO request) {
        return ResponseEntity.ok(new DriverDTO());
    }
}