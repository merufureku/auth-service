package com.merufureku.aromatica.auth_service.services.factory;

import com.merufureku.aromatica.auth_service.services.impl.AuthServiceImpl0;
import com.merufureku.aromatica.auth_service.services.impl.AuthServiceImpl1;
import com.merufureku.aromatica.auth_service.services.interfaces.IAuthService;
import org.springframework.stereotype.Component;

@Component
public class AuthServiceFactory {

    private final AuthServiceImpl0 authServiceImpl0;
    private final AuthServiceImpl1 authServiceImpl1;

    public AuthServiceFactory(AuthServiceImpl0 authServiceImpl0, AuthServiceImpl1 authServiceImpl1) {
        this.authServiceImpl0 = authServiceImpl0;
        this.authServiceImpl1 = authServiceImpl1;
    }

    public IAuthService getService(int version) {
        return switch (version) {
            case 0 -> authServiceImpl0;
            case 1 -> authServiceImpl1;
            default -> throw new IllegalArgumentException("Unsupported service version: " + version);
        };
    }

}
