package com.uberplus.backend.dto.user;

import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class UserProfileDTO {
    protected Integer id;
    protected String email;
    protected String firstName;
    protected String lastName;
    protected String phoneNumber;
    protected String address;
    protected String profilePicture;
    protected UserRole role;
    protected boolean blocked;
    protected String blockReason;
    protected boolean activated;

    public UserProfileDTO(User user, String avatarUrl) {
        id = user.getId();
        email = user.getEmail();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        phoneNumber = user.getPhoneNumber();
        address = user.getAddress();
        profilePicture = avatarUrl;
        role = user.getRole();
        blocked = user.isBlocked();
        blockReason = user.getBlockReason();
        activated = user.isActivated();
    }
}
