package com.example.mobileapp.features.shared.api.dto;

public class PriceEstimateResponse {
    private double finalPrice;
    private String priceDisplay;

    public double getFinalPrice() {
        return finalPrice;
    }

    public String getPriceDisplay() {
        return priceDisplay;
    }
}
