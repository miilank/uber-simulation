package com.uberplus.backend.controller;

import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.user.UserUpdateDTO;
import com.uberplus.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // GET /api/users/profile
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile() {
        return ResponseEntity.ok(new UserProfileDTO());
    }

    // PUT /api/users/profile
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(@Valid @RequestBody UserUpdateDTO update) {
        return ResponseEntity.ok(new UserProfileDTO());
    }
}