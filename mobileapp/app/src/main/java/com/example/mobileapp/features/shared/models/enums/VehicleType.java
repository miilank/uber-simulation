package com.example.mobileapp.features.shared.models.enums;

public enum VehicleType {
    STANDARD("Standard"),
    LUXURY("Luxury"),
    VAN("Van");

    private final String displayName;
    public String getDisplayName() {
        return displayName;
    }

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public static VehicleType fromDisplayName(String displayName) {
        for (VehicleType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return null;
    }
}
