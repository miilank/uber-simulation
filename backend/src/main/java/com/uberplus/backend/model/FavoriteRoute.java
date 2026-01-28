package com.uberplus.backend.model;

import com.uberplus.backend.model.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "favorite_routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRoute {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Passenger user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "start_location_id", nullable = false)
    private Location startLocation;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "end_location_id", nullable = false)
    private Location endLocation;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "favorite_route_waypoints",
            joinColumns = @JoinColumn(name = "route_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    @OrderColumn(name = "order_index")
    private List<Location> waypoints = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column
    private VehicleType vehicleType;

    @Column(nullable = false)
    private boolean babyFriendly;

    @Column(nullable = false)
    private boolean petsFriendly;

    @Column
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}

