package com.uberplus.backend.model;

import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rides")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Passenger creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status = RideStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "start_location_id", nullable = false)
    private Location startLocation;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "end_location_id", nullable = false)
    private Location endLocation;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "ride_waypoints",
            joinColumns = @JoinColumn(name = "ride_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    @OrderColumn(name = "order_index")
    private List<Location> waypoints = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "ride_passengers",
            joinColumns = @JoinColumn(name = "ride_id"),
            inverseJoinColumns = @JoinColumn(name = "passenger_id")
    )
    private List<Passenger> passengers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private boolean babyFriendly;

    @Column(nullable = false)
    private boolean petsFriendly;

    @Column
    private LocalDateTime scheduledTime;

    @Column
    private LocalDateTime estimatedStartTime;

    @Column
    private LocalDateTime estimatedEndTime;

    @Column
    private LocalDateTime actualStartTime;

    @Column
    private LocalDateTime actualEndTime;

    @Column
    private Double distanceKm;

    @Column
    private Double basePrice;

    @Column
    private Double totalPrice;

    @Column
    private String cancelledBy;

    @Column
    private String cancellationReason;

    @Column
    private LocalDateTime cancellationTime;

    @Column(nullable = false)
    private boolean panicActivated = false;

    @Column
    private String panicActivatedBy;

    @Column
    private LocalDateTime panicActivatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stopped_location_id")
    private Location stoppedLocation;

    @Column
    private LocalDateTime stoppedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}

