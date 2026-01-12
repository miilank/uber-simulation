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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<UserProfileDTO> register(@Valid @RequestBody RegisterRequestDTO request) {

        UserProfileDTO createdUser = authService.register(request);

        return ResponseEntity.ok(createdUser);
    }

    // GET /api/auth/activate
    @GetMapping("/activate")
    public ResponseEntity<AuthResponse> activateAccount(@RequestParam String token) {
        AuthResponse response = authService.activate(token);
        System.out.println(response.getToken());
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageDTO> forgotPassword(@RequestBody @Valid PasswordResetRequestDTO request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(new MessageDTO("Reset link sent to your email",true));
    }

    // POST /api/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<MessageDTO> resetPassword(@RequestBody @Valid PasswordResetDTO request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(new MessageDTO("Password reset successful", true));
    }


}



