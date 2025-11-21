package com.merufureku.aromatica.auth_service.utilities;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateUtility {

    public static boolean isDateExpired(LocalDateTime dateTime) {
        // Get the current date-time minus 5 minutes
        LocalDateTime nowMinus5Min = LocalDateTime.now().minusMinutes(5);

        // Check if the given date-time is before nowMinus5Min
        return dateTime.isBefore(nowMinus5Min);
    }
}
