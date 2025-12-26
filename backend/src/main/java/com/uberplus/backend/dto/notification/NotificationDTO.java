package com.uberplus.backend.dto.notification;

import com.uberplus.backend.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Integer id;
    private NotificationType type;
    private String message;
    private Integer rideId;
    private boolean read;
    private LocalDateTime createdAt;
}
