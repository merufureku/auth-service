package com.merufureku.aromatica.auth_service.dto.responses;

public record LoginResponse(Integer id, Token token) {

    public record Token(String accessToken, String refreshToken){}
}
