package com.uberplus.backend.model;

import com.uberplus.backend.dto.user.UserProfileDTO;
import com.uberplus.backend.dto.user.UserUpdateDTO;
import com.uberplus.backend.model.enums.ProfileUpdateStatus;
import com.uberplus.backend.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String profilePicture;

    @Column
    private ProfileUpdateStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ProfileChangeRequest(UserUpdateDTO dto, Driver driver) {
        this.driver = driver;
        this.firstName = dto.getFirstName();
        this.lastName = dto.getLastName();
        this.address = dto.getAddress();
        this.phoneNumber = dto.getPhoneNumber();
        this.profilePicture = dto.getProfilePicture();
        this.status = ProfileUpdateStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}