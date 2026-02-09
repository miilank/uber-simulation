package com.uberplus.backend.model;

import com.uberplus.backend.model.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private Double basePrice;

    @Column(nullable = false)
    private Double pricePerKm;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column
    private String updatedBy;
}