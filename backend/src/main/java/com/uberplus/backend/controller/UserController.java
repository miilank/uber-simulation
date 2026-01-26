package com.uberplus.backend.controller;

import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.user.ChangePasswordDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import com.uberplus.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/profile
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDTO profile = userService.getByEmail(email);
        return ResponseEntity.ok(profile);
    }

    // PUT /api/users/profile
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(Authentication authentication, @Valid @RequestBody UserUpdateRequestDTO update) {
        UserProfileDTO saved = userService.updateProfile(authentication.getName(), update);
        return ResponseEntity.ok(saved);
    }

    // PUT /api/users/change-password
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    public ResponseEntity<MessageDTO> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordDTO request) {
        MessageDTO response = userService.changePassword(authentication.getName(), request);

        return ResponseEntity.ok(response);
    }
}