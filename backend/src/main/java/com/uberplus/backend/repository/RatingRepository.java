package com.uberplus.backend.repository;

import com.uberplus.backend.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
    List<Rating> findByRideId(Integer rideId);
    boolean existsByRideIdAndPassengerId(Integer rideId, Integer passengerId);
}
