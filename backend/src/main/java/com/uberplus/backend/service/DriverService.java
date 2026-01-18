package com.uberplus.backend.service;

import com.uberplus.backend.dto.driver.DriverProfileDTO;
import com.uberplus.backend.dto.user.UserUpdateDTO;

public interface DriverService {
    DriverProfileDTO getProfile(String email);

    void requestProfileUpdate(String email, UserUpdateDTO update);
    void approveProfileUpdate(Integer driverId);

    void rejectProfileUpdate(Integer driverId);
}
