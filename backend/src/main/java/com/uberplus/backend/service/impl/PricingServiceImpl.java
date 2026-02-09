package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.pricing.PricingConfigDTO;
import com.uberplus.backend.dto.pricing.PricingUpdateDTO;
import com.uberplus.backend.model.PricingConfig;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.repository.PricingConfigRepository;
import com.uberplus.backend.service.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PricingConfigRepository pricingConfigRepository;

    @Override
    public double calculatePrice(double distanceKm, VehicleType vehicleType) {
        PricingConfig config = pricingConfigRepository.findByVehicleType(vehicleType)
                .orElseGet(() -> getDefaultPricing(vehicleType));

        double total = config.getBasePrice() + (distanceKm * config.getPricePerKm());
        return Math.max(4.0, Math.ceil(total * 2) / 2.0);
    }

    @Override
    public List<PricingConfigDTO> getAllPricing() {
        List<PricingConfig> configs = pricingConfigRepository.findAll();

        if (configs.isEmpty()) {
            configs = initializeDefaultPricing();
        }

        return configs.stream()
                .map(PricingConfigDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PricingConfigDTO updatePricing(VehicleType vehicleType, PricingUpdateDTO request, String adminEmail) {
        PricingConfig config = pricingConfigRepository.findByVehicleType(vehicleType)
                .orElseGet(() -> {
                    PricingConfig newConfig = new PricingConfig();
                    newConfig.setVehicleType(vehicleType);
                    return newConfig;
                });

        config.setBasePrice(request.getBasePrice());
        config.setPricePerKm(request.getPricePerKm());
        config.setLastUpdated(LocalDateTime.now());
        config.setUpdatedBy(adminEmail);

        PricingConfig saved = pricingConfigRepository.save(config);
        return new PricingConfigDTO(saved);
    }

    private PricingConfig getDefaultPricing(VehicleType vehicleType) {
        PricingConfig config = new PricingConfig();
        config.setVehicleType(vehicleType);

        switch (vehicleType) {
            case STANDARD -> {
                config.setBasePrice(2.5);
                config.setPricePerKm(1.20);
            }
            case LUXURY -> {
                config.setBasePrice(4.5);
                config.setPricePerKm(1.20);
            }
            case VAN -> {
                config.setBasePrice(5.5);
                config.setPricePerKm(1.20);
            }
        }

        config.setLastUpdated(LocalDateTime.now());
        config.setUpdatedBy("system");
        return config;
    }

    private List<PricingConfig> initializeDefaultPricing() {
        return Arrays.stream(VehicleType.values())
                .map(type -> {
                    PricingConfig config = getDefaultPricing(type);
                    return pricingConfigRepository.save(config);
                })
                .collect(Collectors.toList());
    }
}