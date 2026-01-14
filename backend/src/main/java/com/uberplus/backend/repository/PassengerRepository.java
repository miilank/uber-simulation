package com.uberplus.backend.repository;

import org.springframework.stereotype.Repository;
import com.uberplus.backend.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Integer> {
}
