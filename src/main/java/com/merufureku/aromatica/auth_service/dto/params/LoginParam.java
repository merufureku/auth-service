package com.merufureku.aromatica.auth_service.dto.params;

import jakarta.validation.constraints.NotBlank;

public record LoginParam(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
){}
