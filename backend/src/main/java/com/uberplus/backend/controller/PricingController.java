package com.uberplus.backend.controller;

import com.uberplus.backend.dto.pricing.PricingConfigDTO;
import com.uberplus.backend.dto.pricing.PricingUpdateDTO;
import com.uberplus.backend.model.enums.VehicleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    // GET /api/pricing
    @GetMapping
    public ResponseEntity<List<PricingConfigDTO>> getAllPricing() {

        return ResponseEntity.ok(List.of(new PricingConfigDTO()));
    }

    // PUT /api/pricing/{type}
    @PutMapping("/{vehicleType}")
    public ResponseEntity<PricingConfigDTO> updatePricing(@PathVariable VehicleType vehicleType, @Valid @RequestBody PricingUpdateDTO request) {

        return ResponseEntity.ok(new PricingConfigDTO());
    }
}
