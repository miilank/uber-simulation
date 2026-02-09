package com.uberplus.backend.controller;

import com.uberplus.backend.dto.pricing.PricingConfigDTO;
import com.uberplus.backend.dto.pricing.PricingUpdateDTO;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    // GET /api/pricing
    @GetMapping
    public ResponseEntity<List<PricingConfigDTO>> getAllPricing() {
        return ResponseEntity.ok(pricingService.getAllPricing());
    }

    // PUT /api/pricing/STANDARD
    @PutMapping("/{vehicleType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricingConfigDTO> updatePricing(
            Authentication auth,
            @PathVariable VehicleType vehicleType,
            @Valid @RequestBody PricingUpdateDTO request
    ) {
        PricingConfigDTO updated = pricingService.updatePricing(
                vehicleType,
                request,
                auth.getName()
        );
        return ResponseEntity.ok(updated);
    }
}