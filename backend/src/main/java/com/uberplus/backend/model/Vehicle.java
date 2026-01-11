package com.uberplus.backend.model;

import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.model.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Vehicle {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false)
    private int seatCount;

    @Column(nullable = false)
    private boolean babyFriendly;

    @Column(nullable = false)
    private boolean petsFriendly;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.INACTIVE;

    // current location for the map
    // cascade = so that the Location is automatically recorded when we record the Vehicle (without a special repository call)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "current_location_id")
    private Location currentLocation;

    @OneToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
}
