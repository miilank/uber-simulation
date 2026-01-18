package com.uberplus.backend.repository;

import com.uberplus.backend.model.ProfileChangeRequest;
import com.uberplus.backend.model.enums.ProfileUpdateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileChangeRequestRepository extends JpaRepository<ProfileChangeRequest, Integer> {
    Optional<ProfileChangeRequest> findFirstByDriver_IdAndStatusOrderByCreatedAtDesc(Integer id, ProfileUpdateStatus status);
    Optional<ProfileChangeRequest> findByDriver_IdAndStatus(Integer id, ProfileUpdateStatus status);
}
