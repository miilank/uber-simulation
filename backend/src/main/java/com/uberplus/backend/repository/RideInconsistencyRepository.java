package com.uberplus.backend.repository;

import com.uberplus.backend.model.RideInconsistency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideInconsistencyRepository extends JpaRepository<RideInconsistency, Integer> {
    List<RideInconsistency> findByRideId(Integer rideId);
}
