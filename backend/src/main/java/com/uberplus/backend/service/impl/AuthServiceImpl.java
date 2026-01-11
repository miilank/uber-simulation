package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.auth.AuthResponse;
import com.uberplus.backend.dto.auth.RegisterRequestDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.model.Passenger;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.AuthService;
import com.uberplus.backend.service.EmailService;
import com.uberplus.backend.service.JwtService;
import com.uberplus.backend.utils.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserProfileDTO register(RegisterRequestDTO request) {
        if (!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        Passenger user = new Passenger();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAddress(request.getAddress());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfilePicture("default-profile.png");
        user.setRole(UserRole.PASSENGER);
        user.setBlocked(false);
        user.setBlockReason(null);
        user.setActivated(false);

        //activation token generation
        String token = UUID.randomUUID().toString();
        user.setActivationToken(token);
        user.setActivationTokenExpiresAt(LocalDateTime.now().plusHours(24));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        Passenger saved = userRepository.save(user);

        emailService.sendActivationEmail(saved);

        return UserMapper.toUserProfileDTO(saved);
    }

    @Override
    @Transactional
    public AuthResponse activate(String token) {
        User passenger = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

        if (passenger.isActivated()) {
            throw new RuntimeException("Account already activated");
        }
        if (passenger.getActivationTokenExpiresAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token expired");
        }

        passenger.setActivated(true);
        passenger.setActivationToken(null);
        passenger.setActivationTokenExpiresAt(null);
        userRepository.save(passenger);

        return new AuthResponse(jwtService.generateToken(passenger),passenger.getEmail(),passenger.getFirstName());
    }
}
