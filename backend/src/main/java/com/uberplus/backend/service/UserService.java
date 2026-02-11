package com.uberplus.backend.service;

import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.user.ChangePasswordDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.user.UserSearchResultDTO;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import com.uberplus.backend.model.User;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User getByEmail(String email);
    User updateProfile(String email, UserUpdateRequestDTO update, MultipartFile avatar);
    MessageDTO changePassword(String name, @Valid ChangePasswordDTO request);

    Resource getAvatar(Integer id);

    List<User> searchUsers(String searchString, Integer pageSize, Integer pageNumber);

    void blockUser(Integer uuid, String blockReason);
    void unblockUser(Integer uuid);
}
