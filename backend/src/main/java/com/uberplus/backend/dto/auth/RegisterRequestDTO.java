package com.uberplus.backend.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain uppercase, lowercase and number")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 8)
    private String confirmPassword;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank(message = "Address is required")
    @Size(max = 200)
    private String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[1-9][\\d]{0,15}$", message = "Invalid phone number")
    private String phoneNumber;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return password.equals(confirmPassword);
    }
}
