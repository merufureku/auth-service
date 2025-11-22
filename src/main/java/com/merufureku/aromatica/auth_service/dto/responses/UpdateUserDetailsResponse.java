package com.merufureku.aromatica.auth_service.dto.responses;

import com.merufureku.aromatica.auth_service.dao.entity.Users;

import java.time.LocalDateTime;

public record UpdateUserDetailsResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String bio,
        String country,
        String profileImage,
        String coverPhoto,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLogin,
        LocalDateTime lastPasswordSetDt
) {

    public UpdateUserDetailsResponse(Users user) {
        this(
                user.getId().longValue(),
                user.getUsername(),
                user.getUserDetails() != null ? user.getUserDetails().getFirstName() : null,
                user.getUserDetails() != null ? user.getUserDetails().getLastName() : null,
                user.getUserDetails() != null ? user.getUserDetails().getEmail() : null,
                user.getUserDetails() != null ? user.getUserDetails().getPhoneNumber() : null,
                user.getUserDetails() != null ? user.getUserDetails().getBio() : null,
                user.getUserDetails() != null ? user.getUserDetails().getCountry() : null,
                user.getProfileImage(),
                user.getCoverPhoto(),
                user.getCreatedAt(),
                user.getUserDetails().getUpdatedAt(),
                user.getLastLogin(),
                user.getLastPasswordSetDt()
        );
    }
}