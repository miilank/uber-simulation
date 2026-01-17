package com.uberplus.backend.service;

import com.uberplus.backend.dto.driver.DriverProfileDTO;

public interface DriverService {
    DriverProfileDTO getProfile(String email);
}
