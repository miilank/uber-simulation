package com.uberplus.backend.repository;

import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.model.enums.VehicleType;
import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class RideRepositoryTest {
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private UserRepository passengerRepository;
    @Autowired
    private DriverRepository driverRepository;

    @Test
    void saveRideTest() {
        Passenger creator = new Passenger();
        creator.setEmail("p@example.com");
        creator.setPassword("hashed-password");
        creator.setFirstName("Passenger");
        creator.setLastName("Passengerovic");
        creator.setAddress("Street");
        creator.setPhoneNumber("00000000000");
        creator.setRole(UserRole.PASSENGER);
        creator.setBlocked(false);
        creator.setActivated(true);
        creator.setCreatedAt(LocalDateTime.now());
        creator.setUpdatedAt(LocalDateTime.now());
        passengerRepository.save(creator);

        Driver driver = new Driver();
        driver.setEmail("d@example.com");
        driver.setPassword("hashed-password");
        driver.setFirstName("Driver");
        driver.setLastName("Driveric");
        driver.setAddress("Street");
        driver.setPhoneNumber("11111111111");
        driver.setRole(UserRole.DRIVER);
        driver.setActive(true);
        driver.setAvailable(true);
        driver.setCreatedAt(LocalDateTime.now());
        driver.setUpdatedAt(LocalDateTime.now());
        driverRepository.save(driver);

        Location start = new Location();
        start.setLatitude(45.2497);
        start.setLongitude(21.8626);
        start.setAddress("Start Address");
        start.setCreatedAt(LocalDateTime.now());

        Location end = new Location();
        end.setLatitude(41.2052);
        end.setLongitude(23.4642);
        end.setAddress("End Address");
        end.setCreatedAt(LocalDateTime.now());

        Location waypoint = new Location();
        waypoint.setLatitude(44.7900);
        waypoint.setLongitude(20.4500);
        waypoint.setAddress("Waypoint Address");
        waypoint.setCreatedAt(LocalDateTime.now());

        Ride ride = new Ride();
        ride.setCreator(creator);
        ride.setDriver(driver);
        ride.setStartLocation(start);
        ride.setEndLocation(end);
        ride.setWaypoints(Collections.singletonList(waypoint));
        ride.setPassengers(Collections.singletonList(creator));
        ride.setVehicleType(VehicleType.STANDARD);
        ride.setBabyFriendly(false);
        ride.setPetsFriendly(true);
        ride.setStatus(RideStatus.PENDING);
        ride.setBasePrice(200.0);
        ride.setTotalPrice(250.0);
        ride.setCreatedAt(LocalDateTime.now());

        Ride saved = rideRepository.save(ride);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreator()).isNotNull();
        assertThat(saved.getCreator().getId()).isEqualTo(creator.getId());
        assertThat(saved.getDriver()).isNotNull();
        assertThat(saved.getDriver().getId()).isEqualTo(driver.getId());
        assertThat(saved.getStartLocation()).isNotNull();
        assertThat(saved.getEndLocation()).isNotNull();
        Assertions.assertThat(saved.getWaypoints()).hasSize(1);
        Assertions.assertThat(saved.getPassengers()).hasSize(1);
        assertThat(saved.getVehicleType()).isEqualTo(VehicleType.STANDARD);
        assertThat(saved.getStatus()).isEqualTo(RideStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();

    }
}
