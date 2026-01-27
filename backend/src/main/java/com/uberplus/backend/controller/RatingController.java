package com.uberplus.backend.controller;

import com.uberplus.backend.dto.rating.RatingDTO;
import com.uberplus.backend.dto.rating.RatingRequestDTO;
import com.uberplus.backend.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingDTO> submitRating(
            @Valid @RequestBody RatingRequestDTO request,
            Authentication authentication) {

        String email = authentication.getName();
        RatingDTO rating = ratingService.submitRating(request, email);
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<RatingDTO>> getRideRatings(@PathVariable Integer rideId) {
        List<RatingDTO> ratings = ratingService.getRideRatings(rideId);
        return ResponseEntity.ok(ratings);
    }
}