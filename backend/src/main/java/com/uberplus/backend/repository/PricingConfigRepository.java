package com.uberplus.backend.repository;

import com.uberplus.backend.model.PricingConfig;
import com.uberplus.backend.model.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingConfigRepository extends JpaRepository<PricingConfig, Integer> {
    Optional<PricingConfig> findByVehicleType(VehicleType vehicleType);
}