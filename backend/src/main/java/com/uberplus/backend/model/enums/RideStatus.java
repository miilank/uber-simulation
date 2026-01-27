package com.uberplus.backend.model.enums;

public enum RideStatus {
    PENDING,        // created, waiting for assignment
    ACCEPTED,       // driver accepted
    IN_PROGRESS,    // started
    COMPLETED,
    CANCELLED,
    STOPPED,
}