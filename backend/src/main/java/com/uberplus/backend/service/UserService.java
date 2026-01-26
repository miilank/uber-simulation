package com.uberplus.backend.service;

import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.user.ChangePasswordDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import jakarta.validation.Valid;

public interface UserService {
    UserProfileDTO getByEmail(String email);
    UserProfileDTO updateProfile(String email, UserUpdateRequestDTO update);
    MessageDTO changePassword(String name, @Valid ChangePasswordDTO request);
}
