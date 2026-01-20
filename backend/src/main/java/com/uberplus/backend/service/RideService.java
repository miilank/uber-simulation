package com.uberplus.backend.service;

import com.uberplus.backend.dto.ride.CreateRideRequestDTO;
import com.uberplus.backend.dto.ride.RideDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface RideService {
    RideDTO reqestRide(String email, @Valid CreateRideRequestDTO request);
    List<RideDTO> getRides(String email);

    RideDTO startRide(Integer rideId);
}
