package com.uberplus.backend.controller;

import com.uberplus.backend.dto.vehicle.VehicleMapDTO;
import com.uberplus.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // GET /api/vehicles/map
    @GetMapping("/map")
    public ResponseEntity<List<VehicleMapDTO>> getVehiclesForMap() {
        return ResponseEntity.ok(vehicleService.getVehiclesForMap());
    }
}
