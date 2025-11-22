package com.merufureku.aromatica.auth_service.dto.params;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordParam(

        @NotBlank
        String oldPassword,

        @NotBlank
        String newPassword,

        @NotBlank
        String confirmPassword
) {}
