package com.merufureku.aromatica.auth_service.services.interfaces;

import com.merufureku.aromatica.auth_service.dto.params.*;
import com.merufureku.aromatica.auth_service.dto.responses.*;

public interface IAuthService {

    BaseResponse<RegisterResponse> register(RegisterParam registerParam, BaseParam baseParam);

    BaseResponse<LoginResponse> login(LoginParam loginParam, BaseParam baseParam);

    boolean logout(Integer id, BaseParam baseParam);

    BaseResponse<MyDetailsResponse> myDetails(Integer id, BaseParam baseParam);

    BaseResponse<UpdateUserDetailsResponse> updateProfile(Integer id, UpdateUserDetailsParam updateUserDetailsParam, BaseParam baseParam);

    boolean changePassword(Integer id, ChangePasswordParam changePasswordParam, BaseParam baseParam);
}
