package com.uberplus.backend.service;

import com.uberplus.backend.dto.auth.AuthResponse;
import com.uberplus.backend.dto.auth.LoginRequestDTO;
import com.uberplus.backend.dto.auth.PasswordResetDTO;
import com.uberplus.backend.dto.auth.RegisterRequestDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    UserProfileDTO register(RegisterRequestDTO request, MultipartFile avatar);
    AuthResponse activate(String token);
    AuthResponse login(LoginRequestDTO request);
    void requestPasswordReset(String email);
    void confirmPasswordReset(PasswordResetDTO dto);
}
