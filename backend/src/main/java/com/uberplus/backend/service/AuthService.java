package com.uberplus.backend.service;

import com.uberplus.backend.dto.auth.AuthResponse;
import com.uberplus.backend.dto.auth.LoginRequestDTO;
import com.uberplus.backend.dto.auth.PasswordResetDTO;
import com.uberplus.backend.dto.auth.RegisterRequestDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;

public interface AuthService {

    UserProfileDTO register(RegisterRequestDTO request);
    AuthResponse activate(String token);
    AuthResponse login(LoginRequestDTO request);
    void requestPasswordReset(String email);
    void confirmPasswordReset(PasswordResetDTO dto);
}
