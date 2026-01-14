package com.uberplus.backend.repository;

import com.uberplus.backend.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RideRepository extends JpaRepository<Ride, Integer>, JpaSpecificationExecutor<Ride> {
}
