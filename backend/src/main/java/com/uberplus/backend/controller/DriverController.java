package com.uberplus.backend.controller;

import com.uberplus.backend.dto.driver.*;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import com.uberplus.backend.model.Driver;
import com.uberplus.backend.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    // POST /api/drivers
    @PostMapping
    public ResponseEntity<Void> createDriver(@Valid @RequestPart(value="user") DriverCreationDTO request,
                                             @RequestPart(value="avatar", required = false) MultipartFile avatar) {
        Integer id = driverService.createDriver(request, avatar);
        URI location = ServletUriComponentsBuilder
                .fromPath("/api/drivers/" + id.toString())
                .build()
                .toUri();
        return ResponseEntity.created(location).build();
    }

    // PUT /api/drivers/activate
    @PutMapping("/activate")
    public ResponseEntity<Void> activateDriver(@Valid @RequestBody DriverActivationDTO dto) {
        driverService.activateDriver(dto);
        return ResponseEntity.noContent().build();
    }

    // GET /api/drivers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DriverDTO> getDriver(@PathVariable Integer id) {;
        Driver driver = driverService.getDriver(id);
        String avatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/{id}/avatar")
                .buildAndExpand(driver.getId())
                .toUriString();
        return ResponseEntity.ok(new DriverDTO(driver, avatarUrl));
    }

    // GET /api/drivers/profile
    @GetMapping("/profile")
    public ResponseEntity<DriverDTO> getProfile(Authentication authentication) {
        Driver driver = driverService.getProfile(authentication.getName());
        String avatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/{id}/avatar")
                .buildAndExpand(driver.getId())
                .toUriString();
        return ResponseEntity.ok(new DriverDTO(driver, avatarUrl));
    }

    // PUT /api/drivers/profile
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(Authentication authentication,
                                              @Valid @RequestPart(name="update") UserUpdateRequestDTO update,
                                              @RequestPart(name="avatar", required=false) MultipartFile avatar) {
        driverService.requestProfileUpdate(authentication.getName(), update, avatar);
        return ResponseEntity.accepted().build();
    }

    // GET /api/drivers/pending-updates
    @GetMapping("/pending-updates")
    public ResponseEntity<List<DriverUpdateDTO>> getPendingUpdates() {
        List<DriverUpdateDTO> pendingUpdates = driverService.getPendingUpdates();
        pendingUpdates.forEach(update -> {
            String oldAvatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/users/{id}/avatar")
                    .buildAndExpand(update.getDriverId())
                    .toUriString();

            String newAvatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/drivers/{id}/new-avatar")
                    .buildAndExpand(update.getDriverId())
                    .toUriString();

            update.setOldProfilePicture(oldAvatarUrl);
            update.setNewProfilePicture(newAvatarUrl);
        });
        return ResponseEntity.ok(pendingUpdates);
    }

    // PUT /api/drivers/{driverId}/approve-update
    @PutMapping("/{driverId}/approve-update")
    public ResponseEntity<Void> approveUpdate(@PathVariable Integer driverId) {
        driverService.approveProfileUpdate(driverId);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/drivers/{driverId}/reject-update
    @PutMapping("/{driverId}/reject-update")
    public ResponseEntity<Void> rejectUpdate(@PathVariable Integer driverId) {
        driverService.rejectProfileUpdate(driverId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/drivers/{driverId}/new-avatar
    @GetMapping("/{driverId}/new-avatar")
    public ResponseEntity<Resource> getNewAvatar(@PathVariable Integer driverId) {
        Resource avatar = driverService.getNewAvatar(driverId);
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

    // PUT /api/drivers/{driverId}/status
    @PutMapping("/{driverId}/status")
    public ResponseEntity<DriverDTO> updateStatus(@PathVariable Integer driverId, @Valid @RequestBody DriverStatusUpdateDTO request) {
        return ResponseEntity.ok(new DriverDTO());
    }
}