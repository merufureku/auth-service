package com.merufureku.aromatica.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeyConfig {

    @Value("${jwt.access.secret.key}")
    private String jwtAccessSecretKey;

    @Value("${jwt.refresh.secret.key}")
    private String jwtRefreshSecretKey;

    public String getJwtAccessSecretKey() {
        return jwtAccessSecretKey;
    }
    
    public String getJwtRefreshSecretKey() {
        return jwtRefreshSecretKey;
    }
}
