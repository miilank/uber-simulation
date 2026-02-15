package com.uberplus.backend.repository;

import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.Passenger;
import com.uberplus.backend.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Integer>, JpaSpecificationExecutor<Ride> {
    List<Ride> findByDriver(Driver driver);
    List<Ride> findAllByPanicActivated(boolean state);

    @Query("""
        SELECT r FROM Ride r
        JOIN r.passengers p
        WHERE LOWER(p.email) = LOWER(:email)
        AND r.status != 'CANCELLED'
        AND r.status != 'COMPLETED'
        """)
    List<Ride> findActiveRidesByPassengerEmail(@Param("email") String email);

    @Query("""
        select r from Ride r
        join r.passengers p
        where p.email = :email
          and r.status = com.uberplus.backend.model.enums.RideStatus.IN_PROGRESS
        order by r.actualStartTime desc nulls last, r.createdAt desc
        limit 1
    """)
    Optional<Ride> findInProgressForPassenger(@Param("email") String email);
}
