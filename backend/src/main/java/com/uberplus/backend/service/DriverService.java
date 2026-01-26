package com.uberplus.backend.service;

import com.uberplus.backend.dto.driver.DriverActivationDTO;
import com.uberplus.backend.dto.driver.DriverCreationDTO;
import com.uberplus.backend.dto.driver.DriverDTO;
import com.uberplus.backend.dto.driver.DriverUpdateDTO;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface DriverService {
    DriverDTO getProfile(String email);

    void requestProfileUpdate(String email, UserUpdateRequestDTO update);
    void approveProfileUpdate(Integer driverId);
    void rejectProfileUpdate(Integer driverId);

    Integer createDriver(@Valid DriverCreationDTO request);
    DriverDTO getDriver(Integer driverId);
    void activateDriver(DriverActivationDTO req);

    List<DriverUpdateDTO> getPendingUpdates();
}
