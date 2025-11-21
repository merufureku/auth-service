package com.merufureku.aromatica.auth_service.exception;

public record ErrorResponse(int status, String error, String message,
                            String path, String timestamp) {}
