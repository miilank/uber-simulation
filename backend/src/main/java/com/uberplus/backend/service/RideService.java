package com.uberplus.backend.service;

import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.notification.PanicNotificationDTO;
import com.uberplus.backend.dto.ride.*;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

public interface RideService {
    RideDTO requestRide(String email, @Valid CreateRideRequestDTO request);
    List<RideDTO> getRides(String email);
    List<RideDTO> getPassengerRides(String email);

    RideDTO startRide(Integer rideId);

    void setPanic(Integer rideId, Integer userId);
    List<PanicNotificationDTO> getPanicNotifications();
    void resolvePanic(Integer rideId);

    RideDTO cancelRide(Integer rideId, String reason, Integer userId);

    RideDTO getInProgressForPassenger(String email);
    RideDTO completeRide(Integer rideId, String driverEmail);
    RideETADTO getRideETA(Integer rideId) throws IOException, InterruptedException;
    RideDTO arrivedAtPickup(Integer rideId);

    RideDTO stopEarly(Integer rideId, LocationDTO dto);
}
