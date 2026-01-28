package com.uberplus.backend.model;

import com.uberplus.backend.dto.user.UserUpdateRequestDTO;
import com.uberplus.backend.model.enums.ProfileUpdateStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Entity
@Table(name = "profileChangeRequests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private String profilePicture; // Null if no changes

    @Column
    private ProfileUpdateStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ProfileChangeRequest(UserUpdateRequestDTO dto, Driver driver, String avatarFilename) {
        this.driver = driver;
        this.firstName = dto.getFirstName();
        this.lastName = dto.getLastName();
        this.profilePicture = avatarFilename;
        this.address = dto.getAddress();
        this.phoneNumber = dto.getPhoneNumber();
        this.status = ProfileUpdateStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}