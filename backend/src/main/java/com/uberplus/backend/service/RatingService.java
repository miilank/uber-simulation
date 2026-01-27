package com.uberplus.backend.service;

import com.uberplus.backend.dto.rating.RatingDTO;
import com.uberplus.backend.dto.rating.RatingRequestDTO;

import java.util.List;

public interface RatingService {
    RatingDTO submitRating(RatingRequestDTO request, String passengerEmail);
    List<RatingDTO> getRideRatings(Integer rideId);
}
