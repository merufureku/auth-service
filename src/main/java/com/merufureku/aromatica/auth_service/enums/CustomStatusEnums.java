package com.merufureku.aromatica.auth_service.enums;

import org.springframework.http.HttpStatus;

public enum CustomStatusEnums {

    NO_USER_FOUND(4000, "No User Found",HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(4001, "Invalid Token", HttpStatus.UNAUTHORIZED),
    USERNAME_ALREADY_EXIST(4002, "Username already exist", HttpStatus.CONFLICT),
    USER_HAS_NO_ROLES(4003, "Access denied. User does not have the required role.", HttpStatus.FORBIDDEN),
    VALIDATE_ERROR(4005, "Error validating token", HttpStatus.BAD_REQUEST),
    INVALID_TASK_UPDATE_REQUESTOR(4006, "User not authorized to update task", HttpStatus.FORBIDDEN),
    TASK_NOT_FOUND(4040, "Task not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(4041, "Role not found", HttpStatus.NOT_FOUND);

    private int statusCode;
    private String message;
    private HttpStatus httpStatus;

    CustomStatusEnums(int statusCode, String message, HttpStatus httpStatus) {
        this.statusCode = statusCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
