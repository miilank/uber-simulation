package com.uberplus.backend.dto.auth;

import com.uberplus.backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String refreshToken;
    private Integer userId;
    private UserRole role;
    private String firstName;
    private String lastName;
}
