package com.uberplus.backend.service.impl;

import com.uberplus.backend.dto.common.MessageDTO;
import com.uberplus.backend.dto.user.ChangePasswordDTO;
import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.user.UserSearchResultDTO;
import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import com.uberplus.backend.model.User;
import com.uberplus.backend.model.enums.UserRole;
import com.uberplus.backend.repository.UserRepository;
import com.uberplus.backend.service.AvatarService;
import com.uberplus.backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AvatarService avatarService;

    @Override
    @Transactional()
    public User getByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return user;
    }

    @Override
    @Transactional
    public User updateProfile(String email, UserUpdateRequestDTO update, MultipartFile avatar) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (update.getFirstName() != null) {
            user.setFirstName(update.getFirstName().trim());
        }
        if (update.getLastName() != null) {
            user.setLastName(update.getLastName().trim());
        }
        if (update.getPhoneNumber() != null) {
            user.setPhoneNumber(update.getPhoneNumber().trim());
        }
        if (update.getAddress() != null) {
            user.setAddress(update.getAddress().trim());
        }
        if(avatar != null && !avatar.isEmpty()) {
            String old = user.getProfilePicture();

            try {
                String filename = avatarService.storeAvatar(avatar);
                user.setProfilePicture(filename);
            } catch (IOException e) {
                System.out.println("Couldn't store avatar.");
            }

            try {
                avatarService.deleteAvatar(old);
            } catch (IOException e) {
                System.out.println("Couldn't delete avatar.");
            }

        }

        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return saved;
    }

    @Override
    public MessageDTO changePassword(String name, ChangePasswordDTO request) {
        User user = userRepository.findByEmail(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account with this email exists"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        MessageDTO message = new MessageDTO();
        message.setMessage("Password successfully changed.");
        message.setSuccess(true);
        return message;
    }

    @Override
    public Resource getAvatar(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account with this email exists"));

        Resource avatar;
        try {
            avatar = avatarService.getAvatar(user.getProfilePicture());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not get profile picture.");
        }

        return avatar;
    }

    @Override
    public List<User> searchUsers(String searchString, Integer pageSize, Integer pageNumber) {
        pageNumber = pageNumber == null ? 0 : pageNumber;

        // No search
        if (searchString == null || searchString.isBlank()) {
            if (pageSize == null) {
                return userRepository.findAll(Sort.by("firstName").ascending());
            } else {
                Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("firstName").ascending());
                return userRepository.findAll(pageable).getContent();
            }
        }
        // Search
        else if (pageSize == null) {
            return userRepository
                    .findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(
                            searchString, searchString, searchString, Pageable.unpaged()
                    ).getContent();
        } else {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("firstName").ascending());
            return userRepository
                    .findByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(
                            searchString, searchString, searchString, pageable
                    ).getContent();
        }
    }

    @Override
    public void blockUser(Integer uuid, String blockReason) {
        User user = userRepository.findById(uuid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account with this email exists")
        );

        if(user.getRole() == UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot block admin.");
        }

        user.setBlocked(true);
        user.setBlockReason(blockReason);
        userRepository.save(user);
    }

    @Override
    public void unblockUser(Integer uuid) {
        User user = userRepository.findById(uuid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account with this email exists.")
        );

        if(!user.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is not blocked.");
        }

        user.setBlocked(false);
        user.setBlockReason(null);
        userRepository.save(user);
    }
}
