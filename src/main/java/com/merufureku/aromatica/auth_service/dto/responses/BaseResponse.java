package com.merufureku.aromatica.auth_service.dto.responses;

public record BaseResponse<T>(int status, String message, T data){}
