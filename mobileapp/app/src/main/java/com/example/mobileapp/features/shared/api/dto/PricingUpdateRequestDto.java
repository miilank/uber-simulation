package com.example.mobileapp.features.shared.api.dto;

public class PricingUpdateRequestDto {
    private Double basePrice;
    private Double pricePerKm;

    public PricingUpdateRequestDto() {}

    public PricingUpdateRequestDto(Double basePrice, Double pricePerKm) {
        this.basePrice = basePrice;
        this.pricePerKm = pricePerKm;
    }

    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }

    public Double getPricePerKm() { return pricePerKm; }
    public void setPricePerKm(Double pricePerKm) { this.pricePerKm = pricePerKm; }

    public boolean isValid() {
        return basePrice != null && basePrice >= 0
                && pricePerKm != null && pricePerKm >= 0;
    }
}