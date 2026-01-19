package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.ride.CreateRideRequestDTO;
import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.dto.ride.RideDTO;
import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.repository.DriverRepository;
import com.uberplus.backend.repository.RideRepository;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.RideService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class RideServiceImpl implements RideService {
    private RideRepository rideRepository;
    private UserRepository userRepository;
    private DriverRepository driverRepository;

    @Override
    @Transactional
    public RideDTO reqestRide(String email, CreateRideRequestDTO request) {
        Passenger passenger = (Passenger) userRepository.findByEmail(email).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
        );

        List<Driver> availableDrivers = driverRepository.findByActiveTrueAndAvailableTrue();

        if (availableDrivers.isEmpty()) throw new ResponseStatusException(HttpStatus.OK, "No drivers currently available.");

        Driver driver = availableDrivers.getFirst();

        Ride ride = new Ride();
        ride.setCreator(passenger);
        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setStartLocation(request.getStartLocation().toEntity());
        ride.setEndLocation(request.getEndLocation().toEntity());

        List<Location> waypoints = new ArrayList<Location>();

        for(LocationDTO dto : request.getWaypoints()) {
            waypoints.add(dto.toEntity());
        }

        ride.setWaypoints(waypoints);
        ride.setVehicleType(driver.getVehicle().getType());
        ride.setBabyFriendly(request.isBabyFriendly());
        ride.setPetsFriendly(request.isPetFriendly());
        // Passengers, kad bude bilo

        ride.setScheduledTime(request.getScheduledTime());
        // Itd.

        ride.setCreatedAt(LocalDateTime.now());
        rideRepository.save(ride);

        return new RideDTO(ride);
    }
}
