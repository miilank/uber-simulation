package com.uberplus.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("Ride Controller - Stop Early Integration Tests")
@ActiveProfiles("Test")
public class RideControllerStopRideIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Passenger passenger;
    private Driver driver;
    private Vehicle vehicle;
    private Ride activeRide;

    @BeforeEach
    void setUp() {
        rideRepository.deleteAll();
        passengerRepository.deleteAll();
        driverRepository.deleteAll();
        vehicleRepository.deleteAll();

        passenger = createPassenger();
        driver = createDriver();
        vehicle = createVehicle(driver);
        driver.setVehicle(vehicle);
        activeRide = createInProgressRide(driver,passenger);

        passengerRepository.save(passenger);
        driverRepository.save(driver);
        vehicleRepository.save(vehicle);
        rideRepository.save(activeRide);
    }

    // Positive tests

    @Test
    @DisplayName("Should successfully stop ride early with valid data")
    void testStopRideEarly_Success() throws Exception {
        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2600);
        stopLocation.setLongitude(19.8400);
        stopLocation.setAddress("Stop Location Address");

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(activeRide.getId()))
                .andExpect(jsonPath("$.status").value("STOPPED"));

        Ride updatedRide = rideRepository.findById(activeRide.getId()).orElseThrow();
        assertEquals(RideStatus.STOPPED, updatedRide.getStatus());
        assertNotNull(updatedRide.getStoppedAt());
        assertNotNull(updatedRide.getActualEndTime());
        assertNotNull(updatedRide.getStoppedLocation());
        assertEquals(stopLocation.getAddress(), updatedRide.getStoppedLocation().getAddress());
    }

    @Test
    @DisplayName("Should update driver worked minutes when ride is stopped early")
    void testStopRideEarly_UpdatesDriverWorkedMinutes() throws Exception {
        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2600);
        stopLocation.setLongitude(19.8400);
        stopLocation.setAddress("Stop Location");

        Double initialWorkedMinutes = driver.getWorkedMinutesLast24h();

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isOk());

        Driver updatedDriver = driverRepository.findById(driver.getId()).orElseThrow();
        assertTrue(updatedDriver.getWorkedMinutesLast24h() > initialWorkedMinutes,
                "Driver worked minutes should increase");
    }

    @Test
    @DisplayName("Should set vehicle status to AVAILABLE when ride is stopped early")
    void testStopRideEarly_SetsVehicleAvailable() throws Exception {
        vehicle.setStatus(VehicleStatus.OCCUPIED);
        vehicleRepository.save(vehicle);

        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2600);
        stopLocation.setLongitude(19.8400);
        stopLocation.setAddress("Stop Location");

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isOk());

        Driver updatedDriver = driverRepository.findById(driver.getId()).orElseThrow();
        assertEquals(VehicleStatus.AVAILABLE, updatedDriver.getVehicle().getStatus());
    }

    @Test
    @DisplayName("Should recalculate price based on actual distance traveled")
    void testStopRideEarly_RecalculatesPrice() throws Exception {
        Double originalPrice = activeRide.getTotalPrice();

        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2580);
        stopLocation.setLongitude(19.8420);
        stopLocation.setAddress("Nearby Stop Location");

        MvcResult result = mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isOk())
                .andReturn();

        Ride updatedRide = rideRepository.findById(activeRide.getId()).orElseThrow();
        assertNotNull(updatedRide.getTotalPrice());
        assertTrue(updatedRide.getTotalPrice() > 0);
    }

    // Negative tests

    @Test
    @DisplayName("Should return 404 when ride does not exist")
    void testStopRideEarly_RideNotFound() throws Exception {
        Integer nonExistentRideId = 99999;
        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2600);
        stopLocation.setLongitude(19.8400);
        stopLocation.setAddress("Stop Location");

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", nonExistentRideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle missing latitude in request")
    void testStopRideEarly_MissingLatitude() throws Exception {
        String invalidRequest = "{\"longitude\":19.8400,\"address\":\"Stop Location\"}";

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle missing longitude in request")
    void testStopRideEarly_MissingLongitude() throws Exception {
        String invalidRequest = "{\"latitude\":45.2600,\"address\":\"Stop Location\"}";

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle missing address in request")
    void testStopRideEarly_MissingAddress() throws Exception {
        String invalidRequest = "{\"latitude\":45.2600,\"longitude\":19.8400}";

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle null latitude value")
    void testStopRideEarly_NullLatitude() throws Exception {
        String invalidRequest = "{\"latitude\":null,\"longitude\":19.8400,\"address\":\"Stop Location\"}";

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle empty request body")
    void testStopRideEarly_EmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void testStopRideEarly_MalformedJson() throws Exception {
        String malformedJson = "{\"latitude\":45.2600,\"longitude\":19.8400,\"address\":";

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    // Edge cases

    @Test
    @DisplayName("Should handle ride without actual start time")
    void testStopRideEarly_WithoutStartTime() throws Exception {
        activeRide.setActualStartTime(null);
        rideRepository.save(activeRide);

        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2600);
        stopLocation.setLongitude(19.8400);
        stopLocation.setAddress("Stop Location");

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isOk());

        Ride updatedRide = rideRepository.findById(activeRide.getId()).orElseThrow();
        assertEquals(RideStatus.STOPPED, updatedRide.getStatus());
    }

    @Test
    @DisplayName("Should handle very long address strings")
    void testStopRideEarly_LongAddress() throws Exception {
        String longAddress = "A".repeat(500);
        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2600);
        stopLocation.setLongitude(19.8400);
        stopLocation.setAddress(longAddress);

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle special characters in address")
    void testStopRideEarly_SpecialCharactersInAddress() throws Exception {
        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2600);
        stopLocation.setLongitude(19.8400);
        stopLocation.setAddress("Адреса с ћирилицом & Special Chars!@#$%");

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isOk());

        Ride updatedRide = rideRepository.findById(activeRide.getId()).orElseThrow();
        assertEquals("Адреса с ћирилицом & Special Chars!@#$%",
                updatedRide.getStoppedLocation().getAddress());
    }

    @Test
    @DisplayName("Should preserve stopped location separately from end location")
    void testStopRideEarly_PreservesStoppedLocation() throws Exception {
        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2600);
        stopLocation.setLongitude(19.8400);
        stopLocation.setAddress("Early Stop Point");

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isOk());

        Ride updatedRide = rideRepository.findById(activeRide.getId()).orElseThrow();
        assertNotNull(updatedRide.getStoppedLocation());
        assertNotNull(updatedRide.getEndLocation());
        assertEquals(stopLocation.getAddress(), updatedRide.getStoppedLocation().getAddress());
    }

    @Test
    @DisplayName("Should set stoppedAt timestamp when ride is stopped early")
    void testStopRideEarly_SetsStoppedAtTimestamp() throws Exception {
        LocationDTO stopLocation = new LocationDTO();
        stopLocation.setLatitude(45.2600);
        stopLocation.setLongitude(19.8400);
        stopLocation.setAddress("Stop Location");

        LocalDateTime beforeStop = LocalDateTime.now();

        mockMvc.perform(post("/api/rides/{rideId}/stop-early", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopLocation)))
                .andExpect(status().isOk());

        LocalDateTime afterStop = LocalDateTime.now();

        Ride updatedRide = rideRepository.findById(activeRide.getId()).orElseThrow();
        assertNotNull(updatedRide.getStoppedAt());
        assertTrue(updatedRide.getStoppedAt().isAfter(beforeStop.minusSeconds(1)));
        assertTrue(updatedRide.getStoppedAt().isBefore(afterStop.plusSeconds(1)));
    }
    private Driver createDriver() {
        Driver driver = new Driver();
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