package com.merufureku.aromatica.auth_service.dto.params;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordParam(

        @NotBlank(message = "Old password cannot be null")
        String oldPassword,

        @NotBlank(message = "New password cannot be null")
        String newPassword,

        @NotBlank(message = "Confirm password cannot be null")
        String confirmPassword
) {}
