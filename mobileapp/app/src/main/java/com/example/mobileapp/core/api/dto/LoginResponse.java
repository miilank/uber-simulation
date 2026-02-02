package com.example.mobileapp.core.api.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String firstName;

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }
}
