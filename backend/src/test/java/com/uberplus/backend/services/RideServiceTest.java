package com.uberplus.backend.services;

import com.uberplus.backend.dto.ride.CreateRideRequestDTO;
import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.repository.*;
import com.uberplus.backend.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class RideServiceTest {
    @Autowired
    private RideService rideService;

    @MockitoBean
    private RideRepository rideRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private DriverRepository driverRepository;
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
    @MockitoBean
    private NotificationService notificationService;


    @Test
    void blockedUserTest() {
        User u = createPassenger();
        u.setBlocked(true);
        u.setBlockReason("Test");

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));

        CreateRideRequestDTO request = createRequest();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.requestRide("email@mail.com", request)
        );

        assertEquals(HttpStatus.LOCKED, ex.getStatusCode());
        assertEquals("Test", ex.getReason());
    }

    @Test
    void noUserTest() {
        User u = createPassenger();

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));

        CreateRideRequestDTO request = createRequest();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.requestRide("wrong@mail.com", request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found.", ex.getReason());
    }

    @Test
    void noActiveDriversTest() {
        User u = createPassenger();

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));
        Mockito.when(driverRepository.findByActiveTrueAndBlockedFalse()).thenReturn(new ArrayList<Driver>());

        CreateRideRequestDTO request = createRequest();

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.requestRide("email@mail.com", request)
        );

        assertEquals(HttpStatus.OK, ex.getStatusCode());
        assertEquals("No drivers currently active.", ex.getReason());
    }

    @Test
    void noActiveDriversInFutureTest() {
        User u = createPassenger();

        Driver driver = new Driver();
        driver.setWorkedMinutesLast24h(430);

        CreateRideRequestDTO request = createRequest();

        request.setScheduledTime(LocalDateTime.now());
        request.setEstimatedDurationMinutes(120);

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));
        Mockito.when(driverRepository.findByActiveTrueAndBlockedFalse()).thenReturn(List.of(driver));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.requestRide("email@mail.com", request)
        );

        assertEquals(HttpStatus.OK, ex.getStatusCode());
        assertEquals("No drivers will be active during your scheduled time.", ex.getReason());
    }

    @Test
    void driversBusyFutureTest() {
        User u = createPassenger();

        Driver driver = new Driver();
        driver.setWorkedMinutesLast24h(0);

        Ride ride = new Ride();
        ride.setEstimatedStartTime(LocalDateTime.now().plusMinutes(5));
        ride.setEstimatedEndTime(LocalDateTime.now().plusMinutes(65));

        driver.setRides(List.of(ride));

        CreateRideRequestDTO request = createRequest();

        request.setScheduledTime(LocalDateTime.now().plusMinutes(10));
        request.setEstimatedDurationMinutes(30);

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));
        Mockito.when(driverRepository.findByActiveTrueAndBlockedFalse()).thenReturn(List.of(driver));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.requestRide("email@mail.com", request)
        );

        assertEquals(HttpStatus.OK, ex.getStatusCode());
        assertEquals("All drivers are busy during your scheduled time.", ex.getReason());
    }

    static Stream<String> missingRequirementsProvider() {
        return Stream.of(
                "pets",
                "baby",
                "vehicleType",
                "seats"
        );
    }

    @ParameterizedTest
    @MethodSource("missingRequirementsProvider")
    void requirementsMismatchTest(String caseName) {
        User u = createPassenger();

        Driver driver = new Driver();
        driver.setWorkedMinutesLast24h(0);

        Vehicle vehicle = new Vehicle();
        vehicle.setBabyFriendly(false);
        vehicle.setPetsFriendly(false);
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setSeatCount(2);

        driver.setVehicle(vehicle);

        CreateRideRequestDTO request = createRequest();
        request.setLinkedPassengerEmails(new ArrayList<>());

        switch (caseName) {
            case "pets" -> request.setPetFriendly(true);
            case "baby" -> request.setBabyFriendly(true);
            case "vehicleType" -> request.setVehicleType(VehicleType.LUXURY);
            case "seats" -> request.setLinkedPassengerEmails(List.of("a@mail.com", "b@mail.com"));
        }

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));
        Mockito.when(driverRepository.findByActiveTrueAndBlockedFalse()).thenReturn(List.of(driver));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.requestRide("email@mail.com", request)
        );

        assertEquals(HttpStatus.OK, ex.getStatusCode());
        assertEquals("No available drivers meet your requirements.", ex.getReason());
    }

    @Test
    void cantMakeFutureRideTest() throws Exception {
        User u = createPassenger();

        Driver driver = new Driver();
        driver.setWorkedMinutesLast24h(0);

        Vehicle vehicle = new Vehicle();
        vehicle.setBabyFriendly(false);
        vehicle.setPetsFriendly(true);
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setSeatCount(10);

        driver.setVehicle(vehicle);

        Ride ride = new Ride();
        ride.setEstimatedStartTime(LocalDateTime.now().plusMinutes(61));
        ride.setScheduledTime(LocalDateTime.now().plusMinutes(60));
        ride.setEstimatedEndTime(LocalDateTime.now().plusMinutes(65));

        Location rideStartLocation = new Location();
        rideStartLocation.setAddress("Test");
        rideStartLocation.setLongitude(32.0000);
        rideStartLocation.setLatitude((23.0000));

        ride.setStartLocation(rideStartLocation);

        driver.setRides(List.of(ride));

        CreateRideRequestDTO request = createRequest();
        request.setPetFriendly(true);
        request.setScheduledTime(LocalDateTime.now().plusMinutes(30));
        request.setEstimatedDurationMinutes(25); // scheduledEnd = scheduledTime + estimatedDuration

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));
        Mockito.when(driverRepository.findByActiveTrueAndBlockedFalse()).thenReturn(List.of(driver));

        Mockito.when(osrmService.getDurationsMatrix(
                Mockito.anyList(),
                Mockito.anyList()
        )).thenReturn(new double[][] {{1200}}); // 20 min

        // Nova voznja se zavrsava za 50 minuta, vec postojeca pocinje za 60 min i treba 20 minuta izmedju krajnje lokacije
        // nove voznje i pocetne postojece => Vozac ne moze da stigne
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> rideService.requestRide("email@mail.com", request)
        );

        assertEquals(HttpStatus.OK, ex.getStatusCode());
        assertEquals("Cannot assign this ride because the time is incompatible with drivers' schedules (debug message).", ex.getReason());
    }

    @Test
    void successfulWithPrevRidesTest() throws Exception {
        User u = createPassenger();
        u.setId(1);

        User u2 = createPassenger(); //linked passenger
        u2.setId(2);
        u2.setEmail("passenger2@mail.com");
        u2.setFirstName("Passenger2");

        Driver driver1 = new Driver();
        driver1.setWorkedMinutesLast24h(0);

        Vehicle vehicle = new Vehicle();
        vehicle.setBabyFriendly(false);
        vehicle.setPetsFriendly(true);
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setSeatCount(10);

        driver1.setVehicle(vehicle);

        Ride ride1 = new Ride();
        ride1.setEstimatedStartTime(LocalDateTime.now().plusMinutes(31));
        ride1.setScheduledTime(LocalDateTime.now().plusMinutes(30));
        ride1.setEstimatedEndTime(LocalDateTime.now().plusMinutes(35));

        Location rideEndLocation = new Location();
        rideEndLocation.setAddress("Test");
        rideEndLocation.setLongitude(32.0000);
        rideEndLocation.setLatitude((23.0000));

        ride1.setEndLocation(rideEndLocation);

        driver1.setRides(List.of(ride1));

        Driver driver2 = new Driver();
        driver2.setWorkedMinutesLast24h(0);
        driver2.setEmail("nearestdriver@mail.com");
        driver2.setVehicle(vehicle);

        Ride ride2 = new Ride();
        ride2.setEstimatedStartTime(LocalDateTime.now().plusMinutes(31));
        ride2.setScheduledTime(LocalDateTime.now().plusMinutes(30));
        ride2.setEstimatedEndTime(LocalDateTime.now().plusMinutes(35));

        Location rideEndLocation2 = new Location();
        rideEndLocation2.setAddress("Test");
        rideEndLocation2.setLongitude(35.0000);
        rideEndLocation2.setLatitude((35.0000));

        ride2.setEndLocation(rideEndLocation2);

        driver2.setRides(List.of(ride2));

        CreateRideRequestDTO request = createRequest();
        request.setBabyFriendly(false);
        request.setPetFriendly(true);
        request.setVehicleType(VehicleType.STANDARD);
        request.setScheduledTime(LocalDateTime.now().plusMinutes(60));
        request.setEstimatedDurationMinutes(25);

        LocationDTO requestStartLocation = new LocationDTO();
        requestStartLocation.setLongitude(35.1000);
        requestStartLocation.setLatitude(35.1000);

        request.setStartLocation(requestStartLocation);
        request.setLinkedPassengerEmails(List.of("passenger2@mail.com"));

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));
        Mockito.when(userRepository.findByEmail("passenger2@mail.com")).thenReturn(Optional.of(u2));
        Mockito.when(driverRepository.findByActiveTrueAndBlockedFalse()).thenReturn(List.of(driver1, driver2));
        Mockito.when(pricingService.calculatePrice(request.getDistanceKm(), request.getVehicleType())).thenReturn(5.0);
        Mockito.when(osrmService.getDuration(any(), any())).thenReturn(3000.0); // 50 minutes between

        rideService.requestRide("email@mail.com", request);

        ArgumentCaptor<Ride> rideCaptor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository, times(1)).save(rideCaptor.capture());

        Ride saved = rideCaptor.getValue();
        assertEquals(u.getEmail(), saved.getCreator().getEmail());
        assertEquals(driver2.getEmail(), saved.getDriver().getEmail()); // driver2 is closer
        assertEquals(RideStatus.ACCEPTED, saved.getStatus());
        assertEquals(request.getStartLocation().toEntity().getLatitude(), saved.getStartLocation().getLatitude());
        assertEquals(request.getStartLocation().toEntity().getLongitude(), saved.getStartLocation().getLongitude());

        assertEquals(request.getEndLocation().toEntity().getLatitude(), saved.getEndLocation().getLatitude());
        assertEquals(request.getEndLocation().toEntity().getLongitude(), saved.getEndLocation().getLongitude());

        assertEquals(request.getDistanceKm(), saved.getDistanceKm(), 1e-6);
        assertEquals(5.0, saved.getBasePrice(), 1e-6);
        assertEquals(saved.getBasePrice(), saved.getTotalPrice(), 1e-6);
        assertTrue(
                Duration.between(LocalDateTime.now().plusMinutes(35).plusSeconds(3000),
                                            saved.getEstimatedStartTime())
                        .abs().toSeconds() < 10 // 10 seconds lenience because we call LocalDateTime.now()
        );

        assertEquals(request.getVehicleType(), saved.getVehicleType());
        assertEquals(request.isBabyFriendly(), saved.isBabyFriendly());
        assertEquals(request.isPetFriendly(), saved.isPetsFriendly());
        assertTrue(saved.getDriver().getVehicle().getSeatCount() >= saved.getPassengers().size());
        assertEquals(2, saved.getPassengers().size());
        assertEquals(request.getScheduledTime(), saved.getScheduledTime());

        verify(notificationService, times (1)).notifyRideAccepted(any());
    }

    @Test
    void successfulEstStartBeforeScheduledTest() throws Exception {
        User u = createPassenger();
        u.setId(1);

        User u2 = createPassenger(); //linked passenger
        u2.setId(2);
        u2.setEmail("passenger2@mail.com");
        u2.setFirstName("Passenger2");

        Driver driver1 = new Driver();
        driver1.setWorkedMinutesLast24h(0);

        Vehicle vehicle = new Vehicle();
        vehicle.setBabyFriendly(false);
        vehicle.setPetsFriendly(true);
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setSeatCount(10);

        driver1.setVehicle(vehicle);

        Ride ride1 = new Ride();
        ride1.setEstimatedStartTime(LocalDateTime.now().plusMinutes(31));
        ride1.setScheduledTime(LocalDateTime.now().plusMinutes(30));
        ride1.setEstimatedEndTime(LocalDateTime.now().plusMinutes(35));

        Location rideEndLocation = new Location();
        rideEndLocation.setAddress("Test");
        rideEndLocation.setLongitude(32.0000);
        rideEndLocation.setLatitude((23.0000));

        ride1.setEndLocation(rideEndLocation);

        driver1.setRides(List.of(ride1));

        Driver driver2 = new Driver();
        driver2.setWorkedMinutesLast24h(0);
        driver2.setEmail("nearestdriver@mail.com");
        driver2.setVehicle(vehicle);

        Ride ride2 = new Ride();
        ride2.setEstimatedStartTime(LocalDateTime.now().plusMinutes(31));
        ride2.setScheduledTime(LocalDateTime.now().plusMinutes(30));
        ride2.setEstimatedEndTime(LocalDateTime.now().plusMinutes(35));

        Location rideEndLocation2 = new Location();
        rideEndLocation2.setAddress("Test");
        rideEndLocation2.setLongitude(35.0000);
        rideEndLocation2.setLatitude((35.0000));

        ride2.setEndLocation(rideEndLocation2);

        driver2.setRides(List.of(ride2));

        CreateRideRequestDTO request = createRequest();
        request.setBabyFriendly(false);
        request.setPetFriendly(true);
        request.setVehicleType(VehicleType.STANDARD);
        request.setScheduledTime(LocalDateTime.now().plusMinutes(60));
        request.setEstimatedDurationMinutes(25);

        LocationDTO requestStartLocation = new LocationDTO();
        requestStartLocation.setLongitude(35.1000);
        requestStartLocation.setLatitude(35.1000);

        request.setStartLocation(requestStartLocation);
        request.setLinkedPassengerEmails(List.of("passenger2@mail.com"));

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));
        Mockito.when(userRepository.findByEmail("passenger2@mail.com")).thenReturn(Optional.of(u2));
        Mockito.when(driverRepository.findByActiveTrueAndBlockedFalse()).thenReturn(List.of(driver1, driver2));
        Mockito.when(pricingService.calculatePrice(request.getDistanceKm(), request.getVehicleType())).thenReturn(5.0);
        Mockito.when(osrmService.getDuration(any(), any())).thenReturn(300.0); // 5 minutes between

        rideService.requestRide("email@mail.com", request);

        ArgumentCaptor<Ride> rideCaptor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository, times(1)).save(rideCaptor.capture());

        Ride saved = rideCaptor.getValue();
        assertEquals(u.getEmail(), saved.getCreator().getEmail());
        assertEquals(driver2.getEmail(), saved.getDriver().getEmail()); // driver2 is closer
        assertEquals(RideStatus.ACCEPTED, saved.getStatus());
        assertEquals(request.getStartLocation().toEntity().getLatitude(), saved.getStartLocation().getLatitude());
        assertEquals(request.getStartLocation().toEntity().getLongitude(), saved.getStartLocation().getLongitude());

        assertEquals(request.getEndLocation().toEntity().getLatitude(), saved.getEndLocation().getLatitude());
        assertEquals(request.getEndLocation().toEntity().getLongitude(), saved.getEndLocation().getLongitude());

        assertEquals(request.getDistanceKm(), saved.getDistanceKm(), 1e-6);
        assertEquals(5.0, saved.getBasePrice(), 1e-6);
        assertEquals(saved.getBasePrice(), saved.getTotalPrice(), 1e-6);
        assertEquals(request.getScheduledTime(), saved.getEstimatedStartTime());

        assertEquals(request.getVehicleType(), saved.getVehicleType());
        assertEquals(request.isBabyFriendly(), saved.isBabyFriendly());
        assertEquals(request.isPetFriendly(), saved.isPetsFriendly());
        assertTrue(saved.getDriver().getVehicle().getSeatCount() >= saved.getPassengers().size());
        assertEquals(2, saved.getPassengers().size());
        assertEquals(request.getScheduledTime(), saved.getScheduledTime());

        verify(notificationService, times (1)).notifyRideAccepted(any());
    }

    @Test
    void successfulNoPrevRidesTest() throws Exception {
        User u = createPassenger();
        u.setId(1);

        User u2 = createPassenger(); //linked passenger
        u2.setId(2);
        u2.setEmail("passenger2@mail.com");
        u2.setFirstName("Passenger2");

        Driver driver1 = new Driver();
        driver1.setWorkedMinutesLast24h(0);

        Vehicle vehicle = new Vehicle();
        vehicle.setBabyFriendly(false);
        vehicle.setPetsFriendly(true);
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setSeatCount(10);

        Location vh1Loc = new Location();
        vh1Loc.setLatitude(23.0000);
        vh1Loc.setLongitude(23.0000);
        vehicle.setCurrentLocation(vh1Loc);

        driver1.setVehicle(vehicle);

        driver1.setRides(new ArrayList<>());
        driver1.setEmail("testdriver");

        Driver driver2 = new Driver();
        driver2.setWorkedMinutesLast24h(0);
        driver2.setEmail("nearestdriver@mail.com");

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setBabyFriendly(false);
        vehicle2.setPetsFriendly(true);
        vehicle2.setType(VehicleType.STANDARD);
        vehicle2.setSeatCount(10);

        Location vh2Loc = new Location();
        vh2Loc.setLatitude(35.0000);
        vh2Loc.setLongitude(35.0000);
        vehicle2.setCurrentLocation(vh2Loc);

        driver2.setVehicle(vehicle2);

        driver2.setRides(new ArrayList<>());

        CreateRideRequestDTO request = createRequest();
        request.setBabyFriendly(false);
        request.setPetFriendly(true);
        request.setVehicleType(VehicleType.STANDARD);
        request.setScheduledTime(LocalDateTime.now().plusMinutes(60));
        request.setEstimatedDurationMinutes(25);

        LocationDTO requestStartLocation = new LocationDTO();
        requestStartLocation.setLongitude(35.1000);
        requestStartLocation.setLatitude(35.1000);
        request.setStartLocation(requestStartLocation);

        request.setLinkedPassengerEmails(List.of("passenger2@mail.com"));

        Mockito.when(userRepository.findByEmail("email@mail.com")).thenReturn(Optional.of(u));
        Mockito.when(userRepository.findByEmail("passenger2@mail.com")).thenReturn(Optional.of(u2));
        Mockito.when(driverRepository.findByActiveTrueAndBlockedFalse()).thenReturn(List.of(driver1, driver2));
        Mockito.when(pricingService.calculatePrice(request.getDistanceKm(), request.getVehicleType())).thenReturn(5.0);
        Mockito.when(osrmService.getDuration(any(), any())).thenReturn(3000.0); // 50 minutes between

        rideService.requestRide("email@mail.com", request);

        ArgumentCaptor<Ride> rideCaptor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository, times(1)).save(rideCaptor.capture());

        Ride saved = rideCaptor.getValue();
        assertEquals(u.getEmail(), saved.getCreator().getEmail());
        assertEquals(driver2.getEmail(), saved.getDriver().getEmail()); // driver2 is closer
        assertEquals(RideStatus.ACCEPTED, saved.getStatus());
        assertEquals(request.getStartLocation().toEntity().getLatitude(), saved.getStartLocation().getLatitude());
        assertEquals(request.getStartLocation().toEntity().getLongitude(), saved.getStartLocation().getLongitude());

        assertEquals(request.getEndLocation().toEntity().getLatitude(), saved.getEndLocation().getLatitude());
        assertEquals(request.getEndLocation().toEntity().getLongitude(), saved.getEndLocation().getLongitude());

        assertEquals(request.getDistanceKm(), saved.getDistanceKm(), 1e-6);
        assertEquals(5.0, saved.getBasePrice(), 1e-6);
        assertEquals(saved.getBasePrice(), saved.getTotalPrice(), 1e-6);

        assertEquals(request.getVehicleType(), saved.getVehicleType());
        assertEquals(request.isBabyFriendly(), saved.isBabyFriendly());
        assertEquals(request.isPetFriendly(), saved.isPetsFriendly());
        assertTrue(saved.getDriver().getVehicle().getSeatCount() >= saved.getPassengers().size());
        assertEquals(2, saved.getPassengers().size());
        assertEquals(request.getScheduledTime(), saved.getScheduledTime());

        verify(notificationService, times (1)).notifyRideAccepted(any());
    }


    private static User createPassenger() {
        Passenger u = new Passenger();
        u.setId(1);

        u.setEmail("email@mail.com");
        u.setPassword("hashed-password");
        u.setFirstName("Passenger");
        u.setLastName("Passengerovic");
        u.setAddress("Street");
        u.setPhoneNumber("00000000000");
        u.setProfilePicture(null);

        u.setRole(UserRole.PASSENGER);

        u.setBlocked(false);
        u.setBlockReason(null);

        u.setActivated(true);
        u.setActivationToken(null);
        u.setActivationTokenExpiresAt(null);

        u.setPasswordResetToken(null);
        u.setPasswordResetTokenExpiresAt(null);

        LocalDateTime now = LocalDateTime.now();
        u.setCreatedAt(now.minusMinutes(1));
        u.setUpdatedAt(now);

        return u;
    }

    private static CreateRideRequestDTO createRequest() {
        CreateRideRequestDTO request = new CreateRideRequestDTO();

        LocationDTO startLocation = new LocationDTO();
        startLocation.setLatitude(40.0000);
        startLocation.setLongitude(-70.0000);
        startLocation.setAddress("Street");

        LocationDTO endLocation = new LocationDTO();
        endLocation.setLatitude(40.000);
        endLocation.setLongitude(-70.100);
        endLocation.setAddress("Street");

        request.setStartLocation(startLocation);
        request.setEndLocation(endLocation);
        request.setVehicleType(VehicleType.STANDARD);
        request.setBabyFriendly(false);
        request.setPetFriendly(false);
        request.setScheduledTime(LocalDateTime.now().plusHours(1));
        request.setEstimatedDurationMinutes(30);
        request.setDistanceKm(5.5);

        return request;
    }
}

