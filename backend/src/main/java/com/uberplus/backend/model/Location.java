package com.uberplus.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "locations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Location {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public double distanceTo(Location other) {
        // Approximates meter distance
        double lat1 = this.latitude;
        double lon1 = this.longitude;
        double lat2 = other.getLatitude();
        double lon2 = other.getLongitude();

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;
        double avgLat = Math.toRadians((lat1 + lat2) / 2.0);

        double metersPerDegLat = 111_000;
        double metersPerDegLon = 111_000 * Math.cos(avgLat);

        double x = deltaLon * metersPerDegLon;
        double y = deltaLat * metersPerDegLat;

        return Math.sqrt(x*x + y*y);
    }
}
