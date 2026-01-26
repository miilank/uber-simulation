package com.uberplus.backend.controller;

import com.uberplus.backend.dto.vehicle.VehicleMapDTO;
import com.uberplus.backend.dto.vehicle.VehiclePositionUpdateDTO;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // GET /api/vehicles/driver/{email}/map
    @GetMapping("/driver/{email}/map")
    public ResponseEntity<VehicleMapDTO> getDriverVehicleForMap(@PathVariable String email) {
        return ResponseEntity.ok(vehicleService.getDriverVehicleForMap(email));
    }

    // PUT /api/vehicles/{id}/position
    @PutMapping("/{id}/position")
    public ResponseEntity<Void> updateVehiclePosition(
            @PathVariable Integer id,
            @RequestBody VehiclePositionUpdateDTO dto
    ) {
        vehicleService.updateVehiclePosition(id, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateVehicleStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("status");
        VehicleStatus status = VehicleStatus.valueOf(statusStr);
        vehicleService.updateVehicleStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
