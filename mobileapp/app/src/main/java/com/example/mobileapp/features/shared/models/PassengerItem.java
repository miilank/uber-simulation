package com.example.mobileapp.features.shared.models;

public class PassengerItem {

    private final long id;
    private final String name;
    private final String role; // "You" | "Passenger"

    public PassengerItem(long id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}