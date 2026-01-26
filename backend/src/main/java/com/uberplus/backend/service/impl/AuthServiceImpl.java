package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.auth.AuthResponse;
import com.uberplus.backend.dto.auth.LoginRequestDTO;
import com.uberplus.backend.dto.auth.PasswordResetDTO;
import com.uberplus.backend.dto.auth.RegisterRequestDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.model.Passenger;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.AuthService;
import com.uberplus.backend.service.AvatarService;
import com.uberplus.backend.service.EmailService;
import com.uberplus.backend.service.JwtService;
import com.uberplus.backend.utils.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
    private final AvatarService avatarService;

    @Override
    @Transactional
    public UserProfileDTO register(RegisterRequestDTO request, MultipartFile avatar) {
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

        user = userRepository.save(user); // Need to generate id

        if(avatar != null && !avatar.isEmpty()) {
            try {
                String avatarFilename = avatarService.storeAvatar(avatar);
                user.setProfilePicture(avatarFilename);
            } catch (IOException e) {
                user.setProfilePicture("defaultprofile.png");
                System.out.println("Could not store profile picture.");
                System.out.println(e.getMessage());
            }
        } else {
            user.setProfilePicture("defaultprofile.png");
        }

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

    @Override
    public AuthResponse login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("No account with this email exists"));
        if (!user.isActivated()) {
            throw new RuntimeException("Account not activated. Check your email.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt, user.getEmail(), user.getFirstName());
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String resetToken = jwtService.generateToken(user);
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user, resetToken);
    }

    @Override
    public void confirmPasswordReset(PasswordResetDTO dto) {
        User user = userRepository.findByPasswordResetToken(dto.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token expired");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.save(user);
    }
}
