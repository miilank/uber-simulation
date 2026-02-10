package com.uberplus.backend.dto.user;

import com.uberplus.backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResultDTO {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean blocked;
    private String blockReason;
    private String role;
}
