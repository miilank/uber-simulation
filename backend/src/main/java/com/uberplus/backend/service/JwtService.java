package com.uberplus.backend.service;

import com.uberplus.backend.model.User;

public interface JwtService {
    String generateToken(User user);
}
