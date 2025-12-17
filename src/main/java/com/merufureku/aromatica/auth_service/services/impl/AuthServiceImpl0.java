package com.merufureku.aromatica.auth_service.services.impl;

import com.merufureku.aromatica.auth_service.dto.params.*;
import com.merufureku.aromatica.auth_service.dto.responses.*;
import com.merufureku.aromatica.auth_service.services.interfaces.IAuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl0 implements IAuthService {

    @Override
    public BaseResponse<RegisterResponse> register(RegisterParam registerParam, BaseParam baseParam) {
        return null;
    }

    @Override
    public BaseResponse<LoginResponse> login(LoginParam loginParam, BaseParam baseParam) {
        return null;
    }

    @Override
    public boolean logout(Integer id, BaseParam baseParam) {
        return false;
    }

    @Override
    public BaseResponse<MyDetailsResponse> myDetails(Integer id, BaseParam baseParam) {
        return null;
    }

    @Override
    public BaseResponse<UpdateUserDetailsResponse> updateProfile(Integer id, UpdateUserDetailsParam updateUserDetailsParam, BaseParam baseParam) {
        return null;
    }

    @Override
    public boolean deleteAccount(Integer id, BaseParam baseParam) {
        return false;
    }

    @Override
    public boolean changePassword(Integer id, ChangePasswordParam changePasswordParam, BaseParam baseParam) {
        return false;
    }

    @Override
    public BaseResponse<NewAccessTokenResponse> refreshAccessToken(String refreshToken, BaseParam baseParam) {
        return null;
    }
}
