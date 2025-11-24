package com.merufureku.aromatica.auth_service.utilities;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.merufureku.aromatica.auth_service.constants.AuthConstants.ACCESS_TOKEN;
import static com.merufureku.aromatica.auth_service.constants.AuthConstants.REFRESH_TOKEN;

@Component
public class DateUtility {

    public static boolean isDateExpired(LocalDateTime dateTime, String type) {
        var minusDateTime = switch (type) {
            case ACCESS_TOKEN -> dateTime.minusMinutes(5);
            case REFRESH_TOKEN -> dateTime.minusDays(7);
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };

        var nowMinusExpiration = minusDateTime;

        // Check if the given date-time is before nowMinusExpiration
        return dateTime.isBefore(nowMinusExpiration);
    }
}
