package com.merufureku.aromatica.auth_service.services.interfaces;

import com.merufureku.aromatica.auth_service.dto.params.BaseParam;
import com.merufureku.aromatica.auth_service.dto.params.LoginParam;
import com.merufureku.aromatica.auth_service.dto.params.RegisterParam;
import com.merufureku.aromatica.auth_service.dto.responses.BaseResponse;
import com.merufureku.aromatica.auth_service.dto.responses.LoginResponse;
import com.merufureku.aromatica.auth_service.dto.responses.RegisterResponse;

public interface IAuthService {

    BaseResponse<RegisterResponse> register(RegisterParam registerParam, BaseParam baseParam);

    BaseResponse<LoginResponse> login(LoginParam loginParam, BaseParam baseParam);
}
