package com.merufureku.aromatica.auth_service.utilities;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.merufureku.aromatica.auth_service.constants.AuthConstants.*;

@Component
public class DateUtility {

    public static boolean isAccessTokenExpired(LocalDateTime expirationTime, String type) {
        var now = LocalDateTime.now();

        var gracePeriod = switch (type) {
            case ACCESS_TOKEN -> expirationTime.plusMinutes(ACCESS_TOKEN_EXPIRATION_MINUTES);
            case REFRESH_TOKEN -> expirationTime.plusHours(REFRESH_TOKEN_EXPIRATION_DAYS);
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };

        return now.isAfter(gracePeriod);
    }
}
