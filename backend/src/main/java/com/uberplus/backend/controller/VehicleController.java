package com.uberplus.backend.controller;

import com.uberplus.backend.dto.vehicle.VehicleDTO;
import com.uberplus.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleRepository vehicleRepository;

    // GET /api/vehicles/{vehicleId}
    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleDTO> getVehicle(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(new VehicleDTO());
    }
}
