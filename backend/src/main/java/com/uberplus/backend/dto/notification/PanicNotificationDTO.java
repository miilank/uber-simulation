package com.uberplus.backend.dto.notification;

import com.uberplus.backend.dto.ride.LocationDTO;
import com.uberplus.backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanicNotificationDTO {
    private Integer id;
    private Integer rideId;
    private String activatedBy;
    private Integer driverId;
    private LocalDateTime timestamp;
    private UserRole userType;
}
