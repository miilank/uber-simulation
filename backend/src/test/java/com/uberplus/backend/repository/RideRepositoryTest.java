package com.uberplus.backend.repository;

import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.model.enums.VehicleType;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

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

    @Test
    void findInProgressForPassenger_Success() {
        // Pozitivan test - pronalazi IN_PROGRESS voznju za putnika
        Passenger passenger = createAndSavePassenger();
        Driver driver = createAndSaveDriver();

        Ride ride = createRide(passenger, driver, RideStatus.IN_PROGRESS);
        ride.setActualStartTime(LocalDateTime.now());
        rideRepository.save(ride);

        Optional<Ride> found = rideRepository.findInProgressForPassenger("passenger@example.com");

        assertThat(found).hasValueSatisfying(result -> {
            assertThat(result.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);
            assertThat(result.getId()).isEqualTo(ride.getId());
        });
    }

    @Test
    void findInProgressForPassenger_NotFound() {
        // Negativan test - putnik nema IN_PROGRESS voznju
        Passenger passenger = createAndSavePassenger();
        Driver driver = createAndSaveDriver();

        Ride completed = createRide(passenger, driver, RideStatus.COMPLETED);
        rideRepository.save(completed);

        Optional<Ride> found = rideRepository.findInProgressForPassenger("passenger@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void findInProgressForPassenger_NullActualStartTime() {
        // Granicni slucaj - testira da li postoji voznja sa actualStartTime, ne ona sa null
        Passenger passenger = createAndSavePassenger();
        Driver driver = createAndSaveDriver();

        // voznja sa actualStartTime
        Ride withActualStart = createRide(passenger, driver, RideStatus.IN_PROGRESS);
        withActualStart.setActualStartTime(LocalDateTime.now().minusHours(1));
        rideRepository.save(withActualStart);

        // voznja bez actualStartTime
        Ride withoutActualStart = createRide(passenger, driver, RideStatus.IN_PROGRESS);
        withoutActualStart.setActualStartTime(null);
        rideRepository.save(withoutActualStart);

        Optional<Ride> found = rideRepository.findInProgressForPassenger("passenger@example.com");

        assertThat(found).hasValueSatisfying(result -> {
            assertThat(result.getId()).isEqualTo(withActualStart.getId()); // Vraca onu sa actualStartTime
        });
    }

    @Test
    void findInProgressForPassenger_NullEmail() {
        // Izuzetan slucaj - null email parametar
        Optional<Ride> found = rideRepository.findInProgressForPassenger(null);

        assertThat(found).isEmpty();
    }

    // HELPERS
    private Passenger createAndSavePassenger() {
        Passenger passenger = new Passenger();
        passenger.setEmail("passenger@example.com");
        passenger.setPassword("hashed-password");
        passenger.setFirstName("Test");
        passenger.setLastName("Passenger");
        passenger.setAddress("Street");
        passenger.setPhoneNumber("123456789");
        passenger.setRole(UserRole.PASSENGER);
        passenger.setBlocked(false);
        passenger.setActivated(true);
        passenger.setCreatedAt(LocalDateTime.now());
        passenger.setUpdatedAt(LocalDateTime.now());
        return passengerRepository.save(passenger);
    }

    private Driver createAndSaveDriver() {
        Driver driver = new Driver();
        driver.setEmail("driver@example.com");
        driver.setPassword("hashed-password");
        driver.setFirstName("Test");
        driver.setLastName("Driver");
        driver.setAddress("Street");
        driver.setPhoneNumber("987654321");
        driver.setRole(UserRole.DRIVER);
        driver.setBlocked(false);
        driver.setActive(true);
        driver.setActivated(true);
        driver.setCreatedAt(LocalDateTime.now());
        driver.setUpdatedAt(LocalDateTime.now());
        return driverRepository.save(driver);
    }

    private Ride createRide(Passenger passenger, Driver driver, RideStatus status) {
        Location start = new Location();
        start.setLatitude(45.0);
        start.setLongitude(19.0);
        start.setAddress("Start Address");
        start.setCreatedAt(LocalDateTime.now());

        Location end = new Location();
        end.setLatitude(45.1);
        end.setLongitude(19.1);
        end.setAddress("End Address");
        end.setCreatedAt(LocalDateTime.now());

        Ride ride = new Ride();
        ride.setCreator(passenger);
        ride.setDriver(driver);
        ride.setStartLocation(start);
        ride.setEndLocation(end);
        ride.setPassengers(Collections.singletonList(passenger));
        ride.setVehicleType(VehicleType.STANDARD);
        ride.setBabyFriendly(false);
        ride.setPetsFriendly(false);
        ride.setStatus(status);
        ride.setBasePrice(200.0);
        ride.setTotalPrice(250.0);
        ride.setPanicActivated(false);
        ride.setCreatedAt(LocalDateTime.now());

        return ride;
    }
}
