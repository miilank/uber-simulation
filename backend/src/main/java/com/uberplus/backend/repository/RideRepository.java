package com.uberplus.backend.repository;

import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Integer>, JpaSpecificationExecutor<Ride> {
    List<Ride> findByDriver(Driver driver);
}
