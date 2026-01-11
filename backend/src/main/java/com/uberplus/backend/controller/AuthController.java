package com.uberplus.backend.controller;

import com.uberplus.backend.dto.auth.*;
import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(new LoginResponseDTO());
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<UserProfileDTO> register(@Valid @RequestBody RegisterRequestDTO request) {

        UserProfileDTO createdUser = authService.register(request);

        return ResponseEntity.ok(createdUser);
    }

    // GET /api/auth/activate
    @GetMapping("/activate")
    public ResponseEntity<AuthResponse> activateAccount(@RequestParam String token    ) {
        AuthResponse response = authService.activate(token);
        System.out.println(response.getToken());
        return ResponseEntity.ok(response);
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



