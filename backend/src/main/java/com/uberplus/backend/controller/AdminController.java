package com.uberplus.backend.controller;

import com.uberplus.backend.dto.admin.BlockUserDTO;
import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.driver.DriverCreationDTO;
import com.uberplus.backend.dto.driver.DriverProfileDTO;
import com.uberplus.backend.dto.notification.PanicNotificationDTO;
import com.uberplus.backend.repository.DriverRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DriverRepository driverRepository;

    // POST /api/admin/drivers
    @PostMapping("/drivers")
    public ResponseEntity<DriverProfileDTO> createDriver(@Valid @RequestBody DriverCreationDTO request) {
        return ResponseEntity.ok(new DriverProfileDTO());
    }

    // GET /api/admin/drivers
    @GetMapping("/drivers")
    public ResponseEntity<List<DriverProfileDTO>> getAllDrivers() {
        return ResponseEntity.ok(List.of(new DriverProfileDTO()));
    }

    // POST /api/admin/block-user
    @PostMapping("/block-user")
    public ResponseEntity<MessageDTO> blockUser(@Valid @RequestBody BlockUserDTO request) {;
        return ResponseEntity.ok(new MessageDTO());
    }

    // GET /api/admin/panic-notifications
    @GetMapping("/panic-notifications")
    public ResponseEntity<List<PanicNotificationDTO>> getPanicNotifications() {
        List<PanicNotificationDTO> panics = List.of(new PanicNotificationDTO(), new PanicNotificationDTO());
        return ResponseEntity.ok(panics);
    }
}