package com.uberplus.backend.service;

import com.uberplus.backend.model.Ride;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.NotificationType;

import java.time.LocalDateTime;

public interface NotificationSchedulerService {
    void scheduleNotification(User user,
                              Ride ride,
                              NotificationType type,
                              String message,
                              LocalDateTime when);

    boolean cancelScheduledNotification(Integer userId,
                                        Integer rideId,
                                        NotificationType type,
                                        LocalDateTime when);
}
