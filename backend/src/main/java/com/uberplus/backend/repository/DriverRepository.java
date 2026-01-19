package com.uberplus.backend.repository;


import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {
    Optional<Driver> findByEmail(String email);
    List<Driver> findByActiveTrueAndAvailableTrue();
}
