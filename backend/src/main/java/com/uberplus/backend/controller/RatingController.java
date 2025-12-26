package com.uberplus.backend.controller;

import com.uberplus.backend.dto.rating.RatingDTO;
import com.uberplus.backend.dto.rating.RatingRequestDTO;
import com.uberplus.backend.repository.RatingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingRepository ratingRepository;

    // POST /api/ratings
    @PostMapping
    public ResponseEntity<RatingDTO> submitRating(@Valid @RequestBody RatingRequestDTO request) {
        return ResponseEntity.ok(new RatingDTO());
    }

    // GET /api/ratings/ride/{rideId}
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<RatingDTO>> getRideRatings(@PathVariable Integer rideId) {
        return ResponseEntity.ok(List.of(new RatingDTO(), new RatingDTO()));
    }
}
