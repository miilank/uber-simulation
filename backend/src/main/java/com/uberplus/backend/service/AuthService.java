package com.uberplus.backend.service;

import com.uberplus.backend.dto.auth.RegisterRequestDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;

public interface AuthService {

    UserProfileDTO register(RegisterRequestDTO request);

}
