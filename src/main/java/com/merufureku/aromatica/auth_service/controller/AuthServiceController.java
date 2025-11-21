package com.merufureku.aromatica.auth_service.controller;

import com.merufureku.aromatica.auth_service.dto.params.BaseParam;
import com.merufureku.aromatica.auth_service.dto.params.LoginParam;
import com.merufureku.aromatica.auth_service.dto.params.RegisterParam;
import com.merufureku.aromatica.auth_service.dto.responses.BaseResponse;
import com.merufureku.aromatica.auth_service.dto.responses.LoginResponse;
import com.merufureku.aromatica.auth_service.dto.responses.RegisterResponse;
import com.merufureku.aromatica.auth_service.services.interfaces.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth")
public class AuthServiceController {

    private final IAuthService authService;

    public AuthServiceController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public BaseResponse<RegisterResponse> register(@RequestBody RegisterParam registerParam,
                                                   @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                                   @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        BaseParam baseParam = new BaseParam(version, correlationId);

        BaseResponse<RegisterResponse> response = authService.register(registerParam, baseParam);
        return ResponseEntity.ok(response).getBody();
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user")
    public BaseResponse<LoginResponse> login(@RequestBody LoginParam loginParam,
                                             @RequestParam(name = "version", required = false, defaultValue = "1") int version,
                                             @RequestParam(name = "correlationId", required = false, defaultValue = "") String correlationId) {
        BaseParam baseParam = new BaseParam(version, correlationId);

        BaseResponse<LoginResponse> response = authService.login(loginParam, baseParam);
        return ResponseEntity.ok(response).getBody();
    }
}
