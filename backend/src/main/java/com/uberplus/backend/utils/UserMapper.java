package com.uberplus.backend.utils;

import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.UserRole;

public class UserMapper {

    private UserMapper() {
        // utility class
    }

    public static UserProfileDTO toUserProfileDTO(User user) {
        if (user == null) {
            return null;
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAddress(user.getAddress());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setRole(user.getRole() != null ? UserRole.valueOf(user.getRole().name()) : null);
        dto.setBlocked(user.isBlocked());
        dto.setBlockReason(user.getBlockReason());
        dto.setActivated(user.isActivated());

        return dto;
    }
}
