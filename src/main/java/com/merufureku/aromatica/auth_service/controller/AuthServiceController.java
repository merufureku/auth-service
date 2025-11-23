package com.merufureku.aromatica.auth_service.controller;

import com.merufureku.aromatica.auth_service.dto.params.*;
import com.merufureku.aromatica.auth_service.dto.responses.*;
import com.merufureku.aromatica.auth_service.services.interfaces.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthServiceController {

    private final IAuthService authService;

    public AuthServiceController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public BaseResponse<RegisterResponse> register(@Valid @RequestBody RegisterParam registerParam,
                                                   @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                                   @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authService.register(registerParam, baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user")
    public BaseResponse<LoginResponse> login(@Valid @RequestBody LoginParam loginParam,
                                             @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                             @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authService.login(loginParam, baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Logout a user")
    public ResponseEntity<Void> logout(@RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                       @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        authService.logout(getUserId(), baseParam);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Get my details")
    public BaseResponse<MyDetailsResponse> getMyDetails(@RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                                        @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authService.myDetails(getUserId(), baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    @PutMapping("/auth/me")
    @Operation(summary = "Update my details")
    public BaseResponse<UpdateUserDetailsResponse> updateMyDetails(@Valid @RequestBody UpdateUserDetailsParam updateUserDetailsParam,
                                                                   @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                                                   @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authService.updateProfile(getUserId(), updateUserDetailsParam, baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    @PostMapping("/auth/me/change-password")
    @Operation(summary = "Change my password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordParam changePasswordParam,
                                                @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                               @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        authService.changePassword(getUserId(), changePasswordParam, baseParam);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/refresh/access-token")
    @Operation(summary = "Refresh access token")
    public BaseResponse<NewAccessTokenResponse> refreshAccessToken(@RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                                   @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        var baseParam = new BaseParam(version, correlationId);

        var response = authService.refreshAccessToken(getUserId(), baseParam);

        return ResponseEntity.ok(response).getBody();
    }

    private Integer getUserId(){

        var userId = (Long) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        return userId.intValue();
    }
}
