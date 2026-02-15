package com.uberplus.backend.services;

import com.uberplus.backend.dto.ride.RideDTO;
import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.repository.*;
import com.uberplus.backend.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class RideServiceCompleteRideTest {

    @Autowired
    private RideService rideService;

    @MockitoBean
    private RideRepository rideRepository;
    @MockitoBean
    private DriverRepository driverRepository;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PassengerRepository passengerRepository;
    @MockitoBean
    private OSRMService osrmService;
    @MockitoBean
    private RideInconsistencyRepository rideInconsistencyRepository;
    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private PricingService pricingService;

    // POZITIVNI TESTOVI
    @Test
    void completeRideSuccessTest() {
        // uspjesno zavrsavanje voznje sa svim validnim podacima
        // Arrange
        Driver driver = createDriver();
        Passenger passenger = createPassenger();
        Vehicle vehicle = createVehicle(driver);
        driver.setVehicle(vehicle);

        Ride ride = createInProgressRide(driver, passenger);

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));
        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RideDTO result = rideService.completeRide(100, "driver@test.com");

        // Assert
        assertNotNull(result);
        assertEquals(RideStatus.COMPLETED, result.getStatus());

        verify(rideRepository, times(1)).save(any(Ride.class));
        verify(driverRepository, times(1)).save(driver);
        verify(notificationService, times(1)).notifyRideCompleted(ride);
    }

    @Test
    void completeRideVehicleStatusTest() {
        // provjerava da li se status vozila mijenja na AVAILABLE
        Driver driver = createDriver();
        Passenger passenger = createPassenger();
        Vehicle vehicle = createVehicle(driver);
        vehicle.setStatus(VehicleStatus.OCCUPIED);
        driver.setVehicle(vehicle);

        Ride ride = createInProgressRide(driver, passenger);

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));
        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rideService.completeRide(100, "driver@test.com");

        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        verify(driverRepository, times(1)).save(driver);
    }

    @Test
    void completeRideWorkedMinutesTest() {
        // provjerava azuriranja radnih minuta vozaca
        Driver driver = createDriver();
        driver.setWorkedMinutesLast24h(120.0);
        Passenger passenger = createPassenger();
        Vehicle vehicle = createVehicle(driver);
        driver.setVehicle(vehicle);

        Ride ride = createInProgressRide(driver, passenger);
        ride.setActualStartTime(LocalDateTime.now().minusMinutes(30));

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));
        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rideService.completeRide(100, "driver@test.com");

        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());

        Driver savedDriver = driverCaptor.getValue();
        assertTrue(savedDriver.getWorkedMinutesLast24h() > 120.0);
        assertTrue(savedDriver.getWorkedMinutesLast24h() >= 149.0);
        assertTrue(savedDriver.getWorkedMinutesLast24h() <= 151.0);
    }

    @Test
    void completeRideActualEndTimeSetTest() {
        // provjerava da se actualEndTime postavlja na trenutno vrijeme
        Driver driver = createDriver();
        Passenger passenger = createPassenger();
        Vehicle vehicle = createVehicle(driver);
        driver.setVehicle(vehicle);

        Ride ride = createInProgressRide(driver, passenger);

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));
        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        rideService.completeRide(100, "driver@test.com");
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        ArgumentCaptor<Ride> rideCaptor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository).save(rideCaptor.capture());

        Ride savedRide = rideCaptor.getValue();
        assertNotNull(savedRide.getActualEndTime());
        assertTrue(savedRide.getActualEndTime().isAfter(before));
        assertTrue(savedRide.getActualEndTime().isBefore(after));
    }

    // NEGATIVNI TESTOVI
    @Test
    void completeRideNotFoundTest() {
        // pokusaj zavrsavanja nepostojece voznje
        Mockito.when(rideRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.completeRide(999, "driver@test.com")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assert ex.getReason() != null;
        assertTrue(ex.getReason().contains("Ride not found"));

        verify(rideRepository, never()).save(any());
        verify(driverRepository, never()).save(any());
        verify(notificationService, never()).notifyRideCompleted(any());
    }

    @Test
    void completeRideNotDriverTest() {
        // pokusaj zavrsaavnja voznje od strane neovlascenog vozaca
        Driver driver = createDriver();
        Passenger passenger = createPassenger();
        Ride ride = createInProgressRide(driver, passenger);

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.completeRide(100, "wrong@test.com")
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assert ex.getReason() != null;
        assertTrue(ex.getReason().contains("You are not the driver of this ride"));

        verify(rideRepository, never()).save(any());
        verify(driverRepository, never()).save(any());
        verify(notificationService, never()).notifyRideCompleted(any());
    }

    @Test
    void completeRideNotInProgressTest() {
        // pokusaj zavrsavanja voznje koja nije u statusu IN_PROGRESS
        Driver driver = createDriver();
        Passenger passenger = createPassenger();

        Ride ride = createInProgressRide(driver, passenger);
        ride.setStatus(RideStatus.ACCEPTED); // Ne IN_PROGRESS

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.completeRide(100, "driver@test.com")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assert ex.getReason() != null;
        assertTrue(ex.getReason().contains("Ride is not in progress"));

        verify(rideRepository, never()).save(any());
        verify(driverRepository, never()).save(any());
        verify(notificationService, never()).notifyRideCompleted(any());
    }

    @Test
    void completeRideAlreadyCompletedTest() {
        // pokusaj zavrsavanja voznje koja je vec zavrsena
        Driver driver = createDriver();
        Passenger passenger = createPassenger();

        Ride ride = createInProgressRide(driver, passenger);
        ride.setStatus(RideStatus.COMPLETED);

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.completeRide(100, "driver@test.com")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(rideRepository, never()).save(any());
    }

    @Test
    void completeRideCancelledTest() {
        // pokusaj zavrsavanja otkazane voznje
        Driver driver = createDriver();
        Passenger passenger = createPassenger();

        Ride ride = createInProgressRide(driver, passenger);
        ride.setStatus(RideStatus.CANCELLED);

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.completeRide(100, "driver@test.com")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // GRANICNI SLUCAJEVI
    @Test
    void completeRideNoActualStartTimeTest() {
        // voznja nema actualStartTime (radni minuti se ne azuriraju)
        Driver driver = createDriver();
        driver.setWorkedMinutesLast24h(100.0);
        Passenger passenger = createPassenger();
        Vehicle vehicle = createVehicle(driver);
        driver.setVehicle(vehicle);

        Ride ride = createInProgressRide(driver, passenger);
        ride.setActualStartTime(null); // Edge case

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));
        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rideService.completeRide(100, "driver@test.com");

        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());

        assertEquals(100.0, driverCaptor.getValue().getWorkedMinutesLast24h(), 0.01);
    }

    @Test
    void completeRideNoVehicleTest() {
        // vozac nema dodijeljeno vozilo
        Driver driver = createDriver();
        driver.setVehicle(null); // Edge case
        Passenger passenger = createPassenger();

        Ride ride = createInProgressRide(driver, passenger);

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));
        Mockito.when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideDTO result = rideService.completeRide(100, "driver@test.com");

        assertNotNull(result);
        assertEquals(RideStatus.COMPLETED, result.getStatus());
    }

    @Test
    void completeRideNullDriverTest() {
        // voznja nema dodijeljenog driver-a
        Passenger passenger = createPassenger();
        Ride ride = createInProgressRide(null, passenger);
        ride.setDriver(null);

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));

        assertThrows(ResponseStatusException.class, () ->
                rideService.completeRide(100, "driver@test.com")
        );
    }

    // IZUZETNI SLUCAJEVI
    @Test
    void completeRide_DatabaseSaveFailure() {
        // baza podataka ne uspije da sacuva voznju
        Driver driver = createDriver();
        Passenger passenger = createPassenger();
        Vehicle vehicle = createVehicle(driver);
        driver.setVehicle(vehicle);
        Ride ride = createInProgressRide(driver, passenger);

        Mockito.when(rideRepository.findById(100)).thenReturn(Optional.of(ride));
        Mockito.when(rideRepository.save(any(Ride.class)))
                .thenThrow(new DataAccessException("Database connection timeout") {});

        assertThrows(DataAccessException.class, () ->
                rideService.completeRide(100, "driver@test.com")
        );

        verify(driverRepository, never()).save(any());
        verify(notificationService, never()).notifyRideCompleted(any());
    }

    // Helper methods
    private Driver createDriver() {
        Driver driver = new Driver();
        driver.setId(1);
        driver.setEmail("driver@test.com");
        driver.setPassword("hashed");
        driver.setFirstName("Test");
        driver.setLastName("Driver");
        driver.setAddress("Street");
        driver.setPhoneNumber("123456");
        driver.setRole(UserRole.DRIVER);
        driver.setBlocked(false);
        driver.setActive(true);
        driver.setActivated(true);
        driver.setWorkedMinutesLast24h(0.0);
        driver.setCreatedAt(LocalDateTime.now());
        driver.setUpdatedAt(LocalDateTime.now());
        return driver;
    }

    private Passenger createPassenger() {
        Passenger passenger = new Passenger();
        passenger.setId(2);
        passenger.setEmail("passenger@test.com");
        passenger.setPassword("hashed");
        passenger.setFirstName("Test");
        passenger.setLastName("Passenger");
        passenger.setAddress("Street");
        passenger.setPhoneNumber("654321");
        passenger.setRole(UserRole.PASSENGER);
        passenger.setBlocked(false);
        passenger.setActivated(true);
        passenger.setCreatedAt(LocalDateTime.now());
        passenger.setUpdatedAt(LocalDateTime.now());
        return passenger;
    }

    private Vehicle createVehicle(Driver driver) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(10);
        vehicle.setModel("Toyota");
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setLicensePlate("NS-123");
        vehicle.setSeatCount(4);
        vehicle.setBabyFriendly(false);
        vehicle.setPetsFriendly(false);
        vehicle.setStatus(VehicleStatus.OCCUPIED);
        vehicle.setDriver(driver);

        Location location = new Location();
        location.setLatitude(45.0);
        location.setLongitude(19.0);
        location.setAddress("Test");
        location.setCreatedAt(LocalDateTime.now());
        vehicle.setCurrentLocation(location);

        return vehicle;
    }

    private Ride createInProgressRide(Driver driver, Passenger passenger) {
        Ride ride = new Ride();
        ride.setId(100);
        ride.setCreator(passenger);
        ride.setDriver(driver);
        ride.setStatus(RideStatus.IN_PROGRESS);

        Location start = new Location();
        start.setLatitude(45.0);
        start.setLongitude(19.0);
        start.setAddress("Start");
        start.setCreatedAt(LocalDateTime.now());

        Location end = new Location();
        end.setLatitude(45.1);
        end.setLongitude(19.1);
        end.setAddress("End");
        end.setCreatedAt(LocalDateTime.now());

        ride.setStartLocation(start);
        ride.setEndLocation(end);
        ride.setVehicleType(VehicleType.STANDARD);
        ride.setBabyFriendly(false);
        ride.setPetsFriendly(false);
        ride.setBasePrice(500.0);
        ride.setTotalPrice(500.0);
        ride.setScheduledTime(LocalDateTime.now());
        ride.setActualStartTime(LocalDateTime.now().minusMinutes(15));
        ride.setCreatedAt(LocalDateTime.now().minusHours(1));
        ride.getPassengers().add(passenger);

        return ride;
    }
}