package com.uberplus.backend.service.impl;

import com.uberplus.backend.model.Ride;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.NotificationType;
import com.uberplus.backend.repository.RideRepository;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.NotificationSchedulerService;
import com.uberplus.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class NotificationSchedulerServiceImpl implements NotificationSchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final RideRepository rideRepository;

    private final ConcurrentMap<String, ScheduledFuture<?>> scheduled = new ConcurrentHashMap<>();

    @Override
    public void scheduleNotification(User user,
                                     Ride ride,
                                     NotificationType type,
                                     String message,
                                     LocalDateTime when) {

        String key = key(user.getId(), ride.getId(), type, when);
        Runnable task = () -> {
                notificationService.sendNotificationToUser(user, type, message, ride);
                scheduled.remove(key);
        };

        ScheduledFuture<?> future = taskScheduler.schedule(task, when.atZone(ZoneId.systemDefault()).toInstant());
        ScheduledFuture<?> previous = scheduled.put(key, future);
        if (previous != null) previous.cancel(false);
    }

    @Override
    public boolean cancelScheduledNotification(Integer userId,
                                               Integer rideId,
                                               NotificationType type,
                                               LocalDateTime when) {
        String key = key(userId, rideId, type, when);
        ScheduledFuture<?> f = scheduled.remove(key);
        if (f != null) {
            return f.cancel(false);
        }
        return false;
    }
    

    private String key(Integer userId, Integer rideId, NotificationType type, LocalDateTime when) {
        return "ride:" + rideId + ":user:" + userId + ":type:" + type.name() + ":at:" + when.toString();
    }
}

