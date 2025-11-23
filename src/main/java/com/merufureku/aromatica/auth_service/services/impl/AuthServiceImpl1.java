package com.merufureku.aromatica.auth_service.services.impl;

import com.merufureku.aromatica.auth_service.dao.repository.UsersRepository;
import com.merufureku.aromatica.auth_service.dto.params.*;
import com.merufureku.aromatica.auth_service.dto.responses.*;
import com.merufureku.aromatica.auth_service.exception.ServiceException;
import com.merufureku.aromatica.auth_service.helper.AuthServiceHelper;
import com.merufureku.aromatica.auth_service.helper.TokenHelper;
import com.merufureku.aromatica.auth_service.services.interfaces.IAuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthServiceImpl1 implements IAuthService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AuthServiceHelper authServiceHelper;
    private final TokenHelper tokenHelper;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl1(AuthServiceHelper authServiceHelper, TokenHelper tokenHelper, UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.authServiceHelper = authServiceHelper;
        this.tokenHelper = tokenHelper;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public BaseResponse<RegisterResponse> register(RegisterParam registerParam, BaseParam baseParam) {

        logger.info("Registering user with username: {}", registerParam.username());

        if (usersRepository.existsByUsername(registerParam.username())){
            throw new ServiceException(USERNAME_ALREADY_EXIST);
        }

        var users = authServiceHelper.saveUser(registerParam);

        var response = new RegisterResponse(
                users.getId(),
                users.getUsername(),
                users.getUserDetails().getEmail(),
                users.getCreatedAt()
        );

        logger.info("User registered successfully with ID: {}", users.getId());

        return new BaseResponse<>(HttpStatus.OK.value(),
                "Get User Profile Success", response);
    }

    @Override
    public BaseResponse<LoginResponse> login(LoginParam params, BaseParam baseParam) {

        logger.info("Authenticating user with username: {}", params.username());

        var user = usersRepository.findByUsername(params.username())
                .orElseThrow(() -> new ServiceException(NO_USER_FOUND));

        if (!passwordEncoder.matches(params.password(), user.getPassword())){
            logger.info("Invalid password for {}", params.username());
            throw new ServiceException(NO_USER_FOUND);
        }

        tokenHelper.invalidateAllUserToken(user.getId());

        LoginResponse generatedToken = tokenHelper.generateToken(user);

        authServiceHelper.updateLastLoginDate(user);

        logger.info("Authentication success for {}", params.username());

        return new BaseResponse<>(HttpStatus.OK.value(),
                "Authenticate Success", generatedToken);
    }

    @Override
    public boolean logout(Integer id, BaseParam baseParam) {

        logger.info("Logging out user with ID: {}", id);

        tokenHelper.invalidateAllUserToken(id);

        logger.info("User with ID: {} logged out successfully", id);

        return true;
    }

    @Override
    public BaseResponse<MyDetailsResponse> myDetails(Integer id, BaseParam baseParam) {

        logger.info("Fetching details for user with ID: {}", id);

        var user = usersRepository.findById(id)
                .orElseThrow(() -> new ServiceException(NO_USER_FOUND));

        logger.info("Fetched details for user with ID: {} success", id);

        return new BaseResponse<>(HttpStatus.OK.value(),
                "Get User Details Success", new MyDetailsResponse(user));
    }

    @Override
    public BaseResponse<UpdateUserDetailsResponse> updateProfile(Integer id, UpdateUserDetailsParam updateUserDetailsParam, BaseParam baseParam) {

        logger.info("Updating details for user with ID: {}", id);

        var user = usersRepository.findById(id)
                .orElseThrow(() -> new ServiceException(NO_USER_FOUND));

        var updatedUser = authServiceHelper.updateUser(user, updateUserDetailsParam);

        return new BaseResponse<>(HttpStatus.OK.value(),
                "Update Profile Success", new UpdateUserDetailsResponse(updatedUser));
    }

    @Override
    public boolean changePassword(Integer id, ChangePasswordParam changePasswordParam, BaseParam baseParam) {

        logger.info("Changing password for user with ID: {}", id);

        authServiceHelper.validateNewPassword(changePasswordParam);

        var user = usersRepository.findById(id)
                .orElseThrow(() -> new ServiceException(NO_USER_FOUND));

        if (!passwordEncoder.matches(changePasswordParam.oldPassword(), user.getPassword())){
            throw new ServiceException(INVALID_PASSWORD);
        }

        var bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        user.setPassword(bCryptPasswordEncoder.encode(changePasswordParam.newPassword()));

        usersRepository.save(user);

        logger.info("Password changed successfully for user with ID: {}", id);

        return true;
    }

    @Override
    public BaseResponse<NewAccessTokenResponse> refreshAccessToken(Integer userId, BaseParam baseParam) {

        logger.info("Refreshing access token for user with ID: {}", userId);

        var user = usersRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(NO_USER_FOUND));

        var newAccessToken = tokenHelper.generateNewAccessToken(user);

        logger.info("Access token refreshed successfully for user with ID: {}", userId);

        return new BaseResponse<>(HttpStatus.OK.value(),
                "Refresh Access Token Success", new NewAccessTokenResponse(user.getId(), newAccessToken));

    }
}
