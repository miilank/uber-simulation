package com.uberplus.backend.service;

import com.uberplus.backend.dto.ride.CreateRideRequestDTO;
import com.uberplus.backend.dto.ride.RideDTO;
import jakarta.validation.Valid;

public interface RideService {
    RideDTO reqestRide(String name, @Valid CreateRideRequestDTO request);
}
