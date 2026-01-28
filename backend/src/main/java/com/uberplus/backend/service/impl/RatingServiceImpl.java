package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.rating.RatingDTO;
import com.uberplus.backend.dto.rating.RatingRequestDTO;
import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.Passenger;
import com.uberplus.backend.model.Rating;
import com.uberplus.backend.model.Ride;
import com.uberplus.backend.repository.PassengerRepository;
import com.uberplus.backend.repository.RatingRepository;
import com.uberplus.backend.repository.RideRepository;
import com.uberplus.backend.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final RideRepository rideRepository;
    private final PassengerRepository passengerRepository;

    @Override
    @Transactional
    public RatingDTO submitRating(RatingRequestDTO request, String passengerEmail) {
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        Passenger passenger = passengerRepository.findByUserEmail(passengerEmail)
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        Driver driver = ride.getDriver();
        if (driver == null) {
            throw new RuntimeException("Driver not found for this ride");
        }

        // Check if already rated
        if (ratingRepository.existsByRideIdAndPassengerId(request.getRideId(), passenger.getId())) {
            throw new RuntimeException("You have already rated this ride");
        }

        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setPassenger(passenger);
        rating.setDriver(driver);
        rating.setVehicleRating(request.getVehicleRating());
        rating.setDriverRating(request.getDriverRating());
        rating.setComment(request.getComment());
        rating.setCreatedAt(LocalDateTime.now());

        rating = ratingRepository.save(rating);

        return mapToDTO(rating);
    }

    @Override
    public List<RatingDTO> getRideRatings(Integer rideId) {
        return ratingRepository.findByRideId(rideId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private RatingDTO mapToDTO(Rating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setRideId(rating.getRide().getId());

        String driverFirstName = rating.getDriver().getFirstName();
        String driverLastName = rating.getDriver().getLastName();
        dto.setDriverName(driverFirstName + " " + driverLastName);

        dto.setVehicleRating(rating.getVehicleRating());
        dto.setDriverRating(rating.getDriverRating());
        dto.setComment(rating.getComment());
        dto.setCreatedAt(rating.getCreatedAt());
        return dto;
    }
}