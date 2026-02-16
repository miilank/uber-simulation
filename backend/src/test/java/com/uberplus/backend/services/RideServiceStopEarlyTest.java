package com.uberplus.backend.services;

import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.dto.ride.RideDTO;
import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.model.enums.VehicleStatus;
import com.uberplus.backend.model.enums.VehicleType;
import com.uberplus.backend.repository.DriverRepository;
import com.uberplus.backend.repository.RideRepository;
import com.uberplus.backend.service.PricingService;
import com.uberplus.backend.service.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ride Service - Stop Early Unit Tests")
public class RideServiceStopEarlyTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private RideServiceImpl rideService;

    private Ride mockRide;
    private Driver mockDriver;
    private Vehicle mockVehicle;
    private Passenger mockPassenger;
    private Location startLocation;
    private Location endLocation;

    @BeforeEach
    void setUp() {
        mockPassenger = new Passenger();
        mockPassenger.setId(1);
        mockPassenger.setEmail("passenger@test.com");
        mockPassenger.setFirstName("John");
        mockPassenger.setLastName("Doe");

        mockDriver = new Driver();
        mockDriver.setId(2);
        mockDriver.setEmail("driver@test.com");
        mockDriver.setFirstName("Jane");
        mockDriver.setLastName("Smith");
        mockDriver.setWorkedMinutesLast24h(120.0);

        mockVehicle = new Vehicle();
        mockVehicle.setId(1);
        mockVehicle.setModel("Toyota Camry");
        mockVehicle.setType(VehicleType.STANDARD);
        mockVehicle.setStatus(VehicleStatus.OCCUPIED);
        mockVehicle.setDriver(mockDriver);

        mockDriver.setVehicle(mockVehicle);

        startLocation = new Location();
        startLocation.setId(1);
        startLocation.setAddress("Start Address");
        startLocation.setLatitude(45.2551);
        startLocation.setLongitude(19.8451);
        startLocation.setCreatedAt(LocalDateTime.now());

        endLocation = new Location();
        endLocation.setId(2);
        endLocation.setAddress("End Address");
        endLocation.setLatitude(45.2671);
        endLocation.setLongitude(19.8335);
        endLocation.setCreatedAt(LocalDateTime.now());

        mockRide = new Ride();
        mockRide.setId(1);
        mockRide.setCreator(mockPassenger);
        mockRide.setDriver(mockDriver);
        mockRide.setStatus(RideStatus.IN_PROGRESS);
        mockRide.setStartLocation(startLocation);
        mockRide.setEndLocation(endLocation);
        mockRide.setVehicleType(VehicleType.STANDARD);
        mockRide.setActualStartTime(LocalDateTime.now().minusMinutes(10));
        mockRide.setDistanceKm(5.0);
        mockRide.setTotalPrice(10.0);
        mockRide.setBasePrice(10.0);
        mockRide.setCreatedAt(LocalDateTime.now());
    }

    // Positive cases

    @Test
    @DisplayName("Should successfully stop ride and update all fields")
    void testStopEarly_Success() {
        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        RideDTO result = rideService.stopEarly(1, stopLocationDTO);

        assertNotNull(result);
        verify(rideRepository).findById(1);
        verify(pricingService).calculatePrice(anyDouble(), eq(VehicleType.STANDARD));
        verify(rideRepository).save(argThat(ride ->
                ride.getStatus() == RideStatus.STOPPED &&
                        ride.getStoppedAt() != null &&
                        ride.getActualEndTime() != null &&
                        ride.getStoppedLocation() != null
        ));
    }

    @Test
    @DisplayName("Should update driver worked minutes correctly")
    void testStopEarly_UpdatesWorkedMinutes() {
        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        mockRide.setActualStartTime(LocalDateTime.now().minusMinutes(30));

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        rideService.stopEarly(1, stopLocationDTO);

        verify(driverRepository).save(argThat(driver ->
                driver.getWorkedMinutesLast24h() > 120.0
        ));
    }

    @Test
    @DisplayName("Should set vehicle status to AVAILABLE")
    void testStopEarly_SetsVehicleAvailable() {
        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        rideService.stopEarly(1, stopLocationDTO);

        verify(driverRepository).save(argThat(driver ->
                driver.getVehicle().getStatus() == VehicleStatus.AVAILABLE
        ));
    }

    @Test
    @DisplayName("Should calculate new price based on actual distance")
    void testStopEarly_RecalculatesPrice() {
        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        double expectedPrice = 650.0;

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(expectedPrice);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        rideService.stopEarly(1, stopLocationDTO);

        verify(pricingService).calculatePrice(anyDouble(), eq(VehicleType.STANDARD));
        verify(rideRepository).save(argThat(ride ->
                ride.getTotalPrice() == expectedPrice
        ));
    }

    @Test
    @DisplayName("Should update both endLocation and stoppedLocation")
    void testStopEarly_UpdatesLocations() {
        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("New Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        rideService.stopEarly(1, stopLocationDTO);

        verify(rideRepository).save(argThat(ride ->
                ride.getStoppedLocation() != null &&
                        ride.getEndLocation() != null &&
                        ride.getStoppedLocation().getAddress().equals("New Stop Location") &&
                        ride.getEndLocation().getAddress().equals("New Stop Location")
        ));
    }

    // Negative cases

    @Test
    @DisplayName("Should throw exception when ride not found")
    void testStopEarly_RideNotFound() {
        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            rideService.stopEarly(999, stopLocationDTO);
        });

        verify(rideRepository).findById(999);
        verify(rideRepository, never()).save(any());
        verify(driverRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null location DTO gracefully")
    void testStopEarly_NullLocationDTO() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));

        assertThrows(NullPointerException.class, () -> {
            rideService.stopEarly(1, null);
        });
    }

    // Edge cases

    @Test
    @DisplayName("Should handle ride without actualStartTime")
    void testStopEarly_NoActualStartTime() {
        mockRide.setActualStartTime(null);

        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        RideDTO result = rideService.stopEarly(1, stopLocationDTO);

        assertNotNull(result);
        verify(rideRepository).save(any(Ride.class));
        verify(driverRepository).save(argThat(driver ->
                driver.getWorkedMinutesLast24h() == 120.0
        ));
    }

    @Test
    @DisplayName("Should handle driver without vehicle")
    void testStopEarly_DriverWithoutVehicle() {
        mockDriver.setVehicle(null);

        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        RideDTO result = rideService.stopEarly(1, stopLocationDTO);

        assertNotNull(result);
        verify(rideRepository).save(any(Ride.class));
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should handle very short ride duration")
    void testStopEarly_VeryShortRide() {
        mockRide.setActualStartTime(LocalDateTime.now().minusSeconds(30));

        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(20.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        RideDTO result = rideService.stopEarly(1, stopLocationDTO);

        assertNotNull(result);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should handle very long ride duration")
    void testStopEarly_VeryLongRide() {
        mockRide.setActualStartTime(LocalDateTime.now().minusHours(3));
        mockDriver.setWorkedMinutesLast24h(300.0);

        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(30.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        RideDTO result = rideService.stopEarly(1, stopLocationDTO);

        assertNotNull(result);
        verify(driverRepository).save(argThat(driver ->
                driver.getWorkedMinutesLast24h() > 300.0
        ));
    }

    @Test
    @DisplayName("Should set status to STOPPED")
    void testStopEarly_SetsStatusStopped() {
        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        rideService.stopEarly(1, stopLocationDTO);

        verify(rideRepository).save(argThat(ride ->
                ride.getStatus() == RideStatus.STOPPED
        ));
    }

    @Test
    @DisplayName("Should call pricing service with correct parameters")
    void testStopEarly_CallsPricingServiceCorrectly() {
        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        rideService.stopEarly(1, stopLocationDTO);

        verify(pricingService).calculatePrice(anyDouble(), eq(VehicleType.STANDARD));
    }

    @Test
    @DisplayName("Should save both ride and driver")
    void testStopEarly_SavesBothRideAndDriver() {
        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        rideService.stopEarly(1, stopLocationDTO);

        verify(driverRepository, times(1)).save(any(Driver.class));
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    @DisplayName("Should handle zero worked minutes for driver")
    void testStopEarly_ZeroWorkedMinutes() {
        mockDriver.setWorkedMinutesLast24h(0.0);
        mockRide.setActualStartTime(LocalDateTime.now().minusMinutes(15));

        LocationDTO stopLocationDTO = new LocationDTO();
        stopLocationDTO.setLatitude(45.2600);
        stopLocationDTO.setLongitude(19.8400);
        stopLocationDTO.setAddress("Stop Location");

        when(rideRepository.findById(1)).thenReturn(Optional.of(mockRide));
        when(pricingService.calculatePrice(anyDouble(), any(VehicleType.class))).thenReturn(12.0);
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        rideService.stopEarly(1, stopLocationDTO);

        verify(driverRepository).save(argThat(driver ->
                driver.getWorkedMinutesLast24h() > 0.0
        ));
    }
}