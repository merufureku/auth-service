package com.merufureku.aromatica.auth_service.controller;

import com.merufureku.aromatica.auth_service.dto.params.*;
import com.merufureku.aromatica.auth_service.dto.responses.*;
import com.merufureku.aromatica.auth_service.services.factory.AuthServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthServiceController {

    private final AuthServiceFactory authServiceFactory;

    public AuthServiceController(AuthServiceFactory authServiceFactory) {
        this.authServiceFactory = authServiceFactory;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public BaseResponse<RegisterResponse> register(@Valid @RequestBody RegisterParam registerParam,
                                                   @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                                   @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authServiceFactory.getService(version).register(registerParam, baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user")
    public BaseResponse<LoginResponse> login(@Valid @RequestBody LoginParam loginParam,
                                             @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                             @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authServiceFactory.getService(version).login(loginParam, baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Logout a user")
    public ResponseEntity<Void> logout(@RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                       @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        authServiceFactory.getService(version).logout(getUserId(), baseParam);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Get my details")
    public BaseResponse<MyDetailsResponse> getMyDetails(@RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                                        @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authServiceFactory.getService(version).myDetails(getUserId(), baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    @PutMapping("/auth/me")
    @Operation(summary = "Update my details")
    public BaseResponse<UpdateUserDetailsResponse> updateMyDetails(@Valid @RequestBody UpdateUserDetailsParam updateUserDetailsParam,
                                                                   @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                                                   @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authServiceFactory.getService(version).updateProfile(getUserId(), updateUserDetailsParam, baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    @DeleteMapping("/auth/me")
    @Operation(summary = "Delete my account")
    public ResponseEntity<Void> deleteAccount(@RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                              @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        authServiceFactory.getService(version).deleteAccount(getUserId(), baseParam);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/me/change-password")
    @Operation(summary = "Change my password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordParam changePasswordParam,
                                               @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                               @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        authServiceFactory.getService(version).changePassword(getUserId(), changePasswordParam, baseParam);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/refresh/access-token")
    @Operation(summary = "Refresh access token")
    public BaseResponse<NewAccessTokenResponse> refreshAccessToken(@RequestHeader("Authorization") String refreshToken,
                                                                   @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                                                   @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authServiceFactory.getService(version).refreshAccessToken(refreshToken, baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    private Integer getUserId(){

        return (Integer) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
