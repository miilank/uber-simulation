package com.uberplus.backend.service;

import com.uberplus.backend.model.Passenger;

public interface EmailService {
    public void sendActivationEmail(Passenger user);
}
