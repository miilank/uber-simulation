package com.uberplus.backend.controller;

import com.uberplus.backend.dto.auth.*;
import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(new LoginResponseDTO());
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<UserProfileDTO> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(new UserProfileDTO());
    }

    // POST /api/auth/activate
    @PostMapping("/activate")
    public ResponseEntity<MessageDTO> activateAccount(@RequestBody ActivationDTO request) {
        return ResponseEntity.ok(new MessageDTO());
    }

    // POST /api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageDTO> forgotPassword(@RequestBody PasswordResetRequestDTO request) {
        return ResponseEntity.ok(new MessageDTO());
    }

    // POST /api/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<MessageDTO> resetPassword(@RequestBody PasswordResetDTO request) {
        return ResponseEntity.ok(new MessageDTO());
    }


}



