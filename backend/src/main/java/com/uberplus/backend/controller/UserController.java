package com.uberplus.backend.controller;

import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.user.ChangePasswordDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import com.uberplus.backend.model.User;
import com.uberplus.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;

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
        User user = userService.getByEmail(email);
        String avatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/{id}/avatar")
                .buildAndExpand(user.getId())
                .toUriString();

        return ResponseEntity.ok(new UserProfileDTO(user, avatarUrl));
    }

    // PUT /api/users/profile
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(Authentication authentication,
                                                        @Valid @RequestPart("update") UserUpdateRequestDTO update,
                                                        @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        User user = userService.updateProfile(authentication.getName(), update, avatar);
        String avatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/{id}/avatar")
                .buildAndExpand(user.getId())
                .toUriString();

        return ResponseEntity.ok(new UserProfileDTO(user, avatarUrl));
    }

    // GET /api/users/{id}/avatar
    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable Integer id) {
        Resource avatar = userService.getAvatar(id);
        String contentType;
        try {
            contentType = Files.probeContentType(avatar.getFilePath());
        } catch (Exception e) {
            contentType = "application/octet-stream";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));

        return new ResponseEntity<>(avatar, headers, HttpStatus.OK);
    }

    // PUT /api/users/change-password
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    public ResponseEntity<MessageDTO> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordDTO request) {
        MessageDTO response = userService.changePassword(authentication.getName(), request);

        return ResponseEntity.ok(response);
    }
}