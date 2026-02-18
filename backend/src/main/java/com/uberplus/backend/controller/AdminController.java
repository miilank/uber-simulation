package com.uberplus.backend.controller;

import com.uberplus.backend.dto.admin.BlockUserDTO;
import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.driver.DriverCreationDTO;
import com.uberplus.backend.dto.driver.DriverDTO;
import com.uberplus.backend.dto.notification.PanicNotificationDTO;
import com.uberplus.backend.dto.ride.RideDTO;
import com.uberplus.backend.repository.DriverRepository;
import com.uberplus.backend.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.uberplus.backend.dto.admin.DriverListItemDTO;
import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.Ride;
import com.uberplus.backend.model.enums.RideStatus;
import com.uberplus.backend.service.DriverService;
import java.util.stream.Collectors;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DriverService driverService;
    private final DriverRepository driverRepository;
    private final RideService rideService;

    // POST /api/admin/drivers
    @PostMapping("/drivers")
    public ResponseEntity<DriverDTO> createDriver(@Valid @RequestBody DriverCreationDTO request) {
        return ResponseEntity.ok(new DriverDTO());
    }

    // GET /api/admin/drivers
    @GetMapping("/drivers")
    public ResponseEntity<List<DriverDTO>> getAllDrivers() {
        return ResponseEntity.ok(List.of(new DriverDTO()));
    }

    // POST /api/admin/block-user
    @PostMapping("/block-user")
    public ResponseEntity<MessageDTO> blockUser(@Valid @RequestBody BlockUserDTO request) {;
        return ResponseEntity.ok(new MessageDTO());
    }

    // GET /api/admin/panic-notifications
    @GetMapping("/panic-notifications")
    public ResponseEntity<List<PanicNotificationDTO>> getPanicNotifications() {
        List<PanicNotificationDTO> panics = rideService.getPanicNotifications();
        return ResponseEntity.ok(panics);
    }

    @PutMapping("/panic-notifications/{rideId}/resolve")
    public ResponseEntity<Void> resolvePanic(@PathVariable Integer rideId) {
        rideService.resolvePanic(rideId);
        return ResponseEntity.ok().build();
    }

    // GET /api/admin/rides/active
    @GetMapping("/rides/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RideDTO>> getActiveRidesForAdmin() {
        return ResponseEntity.ok(List.of(new RideDTO()));
    }

    // GET /api/admin/rides/{rideId}
    @GetMapping("/rides/{rideId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RideDTO> getRideState(@PathVariable Integer rideId) {
        return ResponseEntity.ok(new RideDTO());
    }

    // GET /api/admin/drivers/all-with-status
    @GetMapping("/drivers/all-with-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DriverListItemDTO>> getAllDriversWithStatus() {
        List<Driver> drivers = driverService.getAllDrivers();

        List<DriverListItemDTO> driverList = drivers.stream()
                .map(DriverListItemDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(driverList);
    }

    // GET /api/admin/drivers/{driverEmail}/rides
    @GetMapping("/drivers/{driverEmail}/rides")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RideDTO>> getDriverRides(@PathVariable String driverEmail) {
        Driver driver = driverService.getDriverByEmail(driverEmail);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        List<Ride> activeRides = driver.getRides().stream()
                .filter(ride -> ride.getStatus() == RideStatus.ACCEPTED ||
                        ride.getStatus() == RideStatus.IN_PROGRESS)
                .toList();

        List<RideDTO> rideDTOs = activeRides.stream()
                .map(RideDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(rideDTOs);
    }
}