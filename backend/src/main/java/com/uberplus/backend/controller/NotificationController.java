package com.uberplus.backend.controller;

import com.uberplus.backend.dto.notification.NotificationDTO;
import com.uberplus.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    // GET /api/notifications
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        return ResponseEntity.ok(List.of(new NotificationDTO(), new NotificationDTO(), new NotificationDTO()
        ));
    }

    // PUT /api/notifications/{id}/read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(new NotificationDTO());
    }
}
