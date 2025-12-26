package com.uberplus.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivationDTO {
    private String token;
    private String password;
    private String confirmPassword;
}
