package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.driver.DriverActivationDTO;
import com.uberplus.backend.dto.driver.DriverCreationDTO;
import com.uberplus.backend.dto.driver.DriverDTO;
import com.uberplus.backend.dto.driver.DriverUpdateDTO;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import com.uberplus.backend.model.*;
import com.uberplus.backend.model.enums.ProfileUpdateStatus;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.repository.DriverRepository;
import com.uberplus.backend.repository.ProfileChangeRequestRepository;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.DriverService;
import com.uberplus.backend.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final ProfileChangeRequestRepository changeRequestRepository;
    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    @Transactional()
    @Override
    public DriverDTO getProfile(String email) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        return new DriverDTO(driver);
    }

    @Override
    public void requestProfileUpdate(String email, UserUpdateRequestDTO update) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        boolean hasPending = changeRequestRepository
                .findByDriver_IdAndStatus(driver.getId(), ProfileUpdateStatus.PENDING)
                .stream()
                .findAny()
                .isPresent();

        if (hasPending) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "There is already a pending change request for this driver.");
        }

        ProfileChangeRequest changeRequest = new ProfileChangeRequest(update, driver);
        changeRequestRepository.save(changeRequest);
    }

    @Transactional()
    @Override
    public void approveProfileUpdate(Integer driverId) {
        ProfileChangeRequest changeRequest = changeRequestRepository
                .findFirstByDriver_IdAndStatusOrderByCreatedAtDesc(driverId, ProfileUpdateStatus.PENDING)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending change request for this driver"));

        if (changeRequest.getStatus() != ProfileUpdateStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.GONE, "Change request is no longer pending.");
        }

        Driver driver = changeRequest.getDriver();
        if (driver == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Driver data missing for this request");
        }

        driver.setFirstName(changeRequest.getFirstName());
        driver.setLastName(changeRequest.getLastName());
        driver.setAddress(changeRequest.getAddress());
        driver.setPhoneNumber(changeRequest.getPhoneNumber());
        driver.setProfilePicture(changeRequest.getProfilePicture());

        driver.setUpdatedAt(LocalDateTime.now());

        changeRequest.setStatus(ProfileUpdateStatus.ACCEPTED);
        changeRequest.setUpdatedAt(LocalDateTime.now());

        driverRepository.save(driver);
        changeRequestRepository.save(changeRequest);

    }

    @Override
    @Transactional()
    public void rejectProfileUpdate(Integer driverId) {
        ProfileChangeRequest changeRequest = changeRequestRepository
                .findFirstByDriver_IdAndStatusOrderByCreatedAtDesc(driverId, ProfileUpdateStatus.PENDING)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending change request for this driver"));

        if (changeRequest.getStatus() != ProfileUpdateStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.GONE, "Change request is no longer pending.");
        }

        changeRequest.setStatus(ProfileUpdateStatus.REJECTED);
        changeRequest.setUpdatedAt(LocalDateTime.now());

        changeRequestRepository.save(changeRequest);
    }

    @Override
    @Transactional()
    public Integer createDriver(DriverCreationDTO request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT ,"User with this email already exists.");
        }

        Driver driver = new Driver();
        driver.setEmail(email);
        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setAddress(request.getAddress());
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setProfilePicture("default-profile.png");
        driver.setRole(UserRole.PASSENGER);
        driver.setBlocked(false);
        driver.setBlockReason(null);
        driver.setActivated(false);

        Vehicle vehicle = new Vehicle();
        vehicle.setModel(request.getVehicle().getModel());
        vehicle.setType(request.getVehicle().getType());
        vehicle.setBabyFriendly(request.getVehicle().isBabyFriendly());
        vehicle.setPetsFriendly(request.getVehicle().isPetsFriendly());
        vehicle.setLicensePlate(request.getVehicle().getLicensePlate());
        vehicle.setSeatCount(request.getVehicle().getSeatCount());

        vehicle.setDriver(driver);
        driver.setVehicle(vehicle);

        String token = UUID.randomUUID().toString();
        driver.setActivationToken(token);
        driver.setActivationTokenExpiresAt(LocalDateTime.now().plusHours(24));
        driver.setPasswordResetToken(null);
        driver.setPasswordResetTokenExpiresAt(null);
        LocalDateTime now = LocalDateTime.now();
        driver.setCreatedAt(now);
        driver.setUpdatedAt(now);

        Driver saved = driverRepository.save(driver);

        emailService.sendDriverActivationEmail(saved);

        return driver.getId();
    }

    @Override
    public DriverDTO getDriver(Integer driverId) {
        Driver driver = driverRepository.findById(driverId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found.")
        );

        return new DriverDTO(driver);
    }

    @Override
    public void activateDriver(DriverActivationDTO req) {
        User driver = userRepository.findByActivationToken(req.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid activation token"));

        if (driver.isActivated()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already activated");
        }
        if (driver.getActivationTokenExpiresAt().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.GONE, "Token expired");
        }

        driver.setActivated(true);
        driver.setActivationToken(null);
        driver.setActivationTokenExpiresAt(null);
        driver.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(driver);
    }

    @Override
    public List<DriverUpdateDTO> getPendingUpdates() {
        List<ProfileChangeRequest> changeRequests = changeRequestRepository.findByStatus(ProfileUpdateStatus.PENDING);
        return changeRequests.stream().map(DriverUpdateDTO::new).toList();
    }
}
