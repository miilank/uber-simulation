package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.driver.DriverProfileDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.model.Driver;
import com.uberplus.backend.model.User;
import com.uberplus.backend.repository.DriverRepository;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.DriverService;
import com.uberplus.backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {


    private final DriverRepository driverRepository;


    @Transactional()
    @Override
    public DriverProfileDTO getProfile(String email) {
        Driver driver = driverRepository.findByEmail(email).orElse(null);
        if(driver == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return new DriverProfileDTO(driver);
    }
}
