package com.merufureku.aromatica.auth_service.dto.params;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserDetailsParam (

    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @Email(message = "Invalid email format")
    String email,

    String phoneNumber,
    String bio,

    String country,

    String profileImage,
    String coverPhoto
){}
