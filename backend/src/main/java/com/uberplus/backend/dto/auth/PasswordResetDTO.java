package com.uberplus.backend.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDTO {
    @NotBlank
    private String token;
    @NotBlank @Size(min = 8) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")
    private String newPassword;
    private String confirmPassword;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return newPassword.equals(confirmPassword);
    }
}