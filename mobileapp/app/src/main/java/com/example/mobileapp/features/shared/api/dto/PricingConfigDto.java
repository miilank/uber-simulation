package com.example.mobileapp.features.shared.api.dto;

import java.time.LocalDateTime;

public class PricingConfigDto {
    private Integer id;
    private VehicleType vehicleType;
    private Double basePrice;
    private Double pricePerKm;
    private LocalDateTime lastUpdated;
    private String updatedBy;

    public PricingConfigDto() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }

    public Double getPricePerKm() { return pricePerKm; }
    public void setPricePerKm(Double pricePerKm) { this.pricePerKm = pricePerKm; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Helper method
    public double calculateExamplePrice(double distanceKm) {
        return basePrice + (distanceKm * pricePerKm);
    }

    // Enum
    public enum VehicleType {
        STANDARD("Standard", "🚘", "Regular sedan cars"),
        LUXURY("Luxury", "🚙", "Premium vehicles"),
        VAN("Van", "🚐", "Large capacity vehicles");

        private final String label;
        private final String icon;
        private final String description;

        VehicleType(String label, String icon, String description) {
            this.label = label;
            this.icon = icon;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
    }
}