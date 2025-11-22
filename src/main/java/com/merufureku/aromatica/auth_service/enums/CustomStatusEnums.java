package com.merufureku.aromatica.auth_service.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomStatusEnums {

    NO_USER_FOUND(4000, "No User Found",HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(4001, "Invalid Token", HttpStatus.UNAUTHORIZED),
    USERNAME_ALREADY_EXIST(4002, "Username already exist", HttpStatus.CONFLICT),
    USER_HAS_NO_ROLES(4003, "Access denied. User does not have the required role.", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(4004, "Role not found", HttpStatus.NOT_FOUND),
    CONFIRM_PASSWORD_MISMATCH(4005, "Confirm password mismatch", HttpStatus.BAD_REQUEST),
    SAME_OLD_AND_NEW_PASSWORD(4006, "Old and new password cannot be the same", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(4007, "Incorrect old password", HttpStatus.BAD_REQUEST);

    private final int statusCode;
    private final String message;
    private final HttpStatus httpStatus;

    CustomStatusEnums(int statusCode, String message, HttpStatus httpStatus) {
        this.statusCode = statusCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
