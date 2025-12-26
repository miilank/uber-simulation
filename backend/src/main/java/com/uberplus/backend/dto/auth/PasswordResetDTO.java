package com.uberplus.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDTO {
    private String token;        // From email link
    private String newPassword;
    private String confirmPassword;
}