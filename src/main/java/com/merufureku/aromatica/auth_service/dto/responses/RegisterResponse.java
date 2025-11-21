package com.merufureku.aromatica.auth_service.dto.responses;

import java.time.LocalDateTime;

public record RegisterResponse(Integer id, String username, String email, LocalDateTime createdAt) {}
