package com.example.mobileapp.features.shared.api.dto;

import java.util.ArrayList;
import java.util.List;

public class GeocodeResult {
    private class NominatimAddress {
        String road;
        String house_number;
        String city;
        String town;
        String village;
        String postcode;
        String country;
        String municipality;
        String suburb;
        String hamlet;
        String state;
    }

    public String getFormattedResult() {
        return formattedResult;
    }

    public String display_name = "";
    public double lat;
    public double lon;
    public NominatimAddress address;
    public String formattedResult;

    public void formatAddress() {
        String road = address.road == null ? "" : address.road.trim();
        String house = address.house_number == null ? "" : address.house_number.trim();

        String streetPart = "";
        if (!road.isEmpty() && !house.isEmpty()) {
            streetPart = road + " " + house;
        } else if (!road.isEmpty()) {
            streetPart = road;
        } else if (!house.isEmpty()) {
            streetPart = house;
        }

        String city = firstNonNull(
                address.city,
                address.town,
                address.village,
                address.municipality,
                address.suburb,
                address.hamlet,
                address.state
        );

        List<String> parts = new ArrayList<>();
        if (!streetPart.isEmpty()) parts.add(streetPart);
        if (city != null && !city.isEmpty()) parts.add(city);

        if (parts.isEmpty()) {
            formattedResult = display_name != null ? display_name.trim() : "";
        }

        formattedResult = String.join(", ", parts);
    }
    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        for (T v : values) {
            if (v != null) return v;
        }
        return null;
    }
}
