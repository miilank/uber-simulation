package com.uberplus.backend.service;

import com.uberplus.backend.model.Passenger;
import com.uberplus.backend.model.User;

public interface EmailService {
    void sendActivationEmail(Passenger user);
    void sendPasswordResetEmail(User user, String token);
}
