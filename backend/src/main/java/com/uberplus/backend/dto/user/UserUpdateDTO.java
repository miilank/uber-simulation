package com.uberplus.backend.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class UserUpdateDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profilePicture;
}
