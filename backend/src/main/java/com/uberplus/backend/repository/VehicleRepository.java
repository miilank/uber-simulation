package com.uberplus.backend.repository;

import com.uberplus.backend.model.Vehicle;
import com.uberplus.backend.model.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    List<Vehicle> findByStatusIn(List<VehicleStatus> statuses);
}
