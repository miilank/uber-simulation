package com.uberplus.backend.service;

import com.uberplus.backend.dto.driver.DriverActivationDTO;
import com.uberplus.backend.dto.driver.DriverCreationDTO;
import com.uberplus.backend.dto.driver.DriverDTO;
import com.uberplus.backend.dto.driver.DriverUpdateDTO;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import com.uberplus.backend.model.Driver;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DriverService {
    Driver getProfile(String email);

    void requestProfileUpdate(String email, UserUpdateRequestDTO update, MultipartFile avatar);
    void approveProfileUpdate(Integer driverId);
    void rejectProfileUpdate(Integer driverId);

    Integer createDriver(@Valid DriverCreationDTO request, MultipartFile avatar);
    Driver getDriver(Integer driverId);
    void activateDriver(DriverActivationDTO req);

    List<DriverUpdateDTO> getPendingUpdates();

    Resource getNewAvatar(Integer driverId);
}
