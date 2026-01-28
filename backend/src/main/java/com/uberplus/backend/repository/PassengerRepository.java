package com.uberplus.backend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.uberplus.backend.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Integer> {

    @Query("SELECT p FROM Passenger p WHERE p.email = :email")
    Optional<Passenger> findByUserEmail(String email);
}
