package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.driver.DriverProfileDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.user.UserUpdateDTO;
import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.ProfileChangeRequest;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.ProfileUpdateStatus;
import com.uberplus.backend.repository.DriverRepository;
import com.uberplus.backend.repository.ProfileChangeRequestRepository;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.DriverService;
import com.uberplus.backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {


    private final DriverRepository driverRepository;
    private final ProfileChangeRequestRepository changeRequestRepository;

    @Transactional()
    @Override
    public DriverProfileDTO getProfile(String email) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        return new DriverProfileDTO(driver);
    }

    @Override
    public void requestProfileUpdate(String email, UserUpdateDTO update) {
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
}
