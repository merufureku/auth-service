package com.merufureku.aromatica.auth_service.services.impl;

import com.merufureku.aromatica.auth_service.dao.entity.Users;
import com.merufureku.aromatica.auth_service.dao.repository.UsersRepository;
import com.merufureku.aromatica.auth_service.dto.params.BaseParam;
import com.merufureku.aromatica.auth_service.dto.params.LoginParam;
import com.merufureku.aromatica.auth_service.dto.params.RegisterParam;
import com.merufureku.aromatica.auth_service.dto.responses.BaseResponse;
import com.merufureku.aromatica.auth_service.dto.responses.LoginResponse;
import com.merufureku.aromatica.auth_service.dto.responses.RegisterResponse;
import com.merufureku.aromatica.auth_service.exception.ServiceException;
import com.merufureku.aromatica.auth_service.helper.AuthServiceHelper;
import com.merufureku.aromatica.auth_service.helper.TokenHelper;
import com.merufureku.aromatica.auth_service.services.interfaces.IAuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.NO_USER_FOUND;
import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.USERNAME_ALREADY_EXIST;

@Service
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
                users.getEmail(),
                users.getCreatedAt()
        );

        logger.info("User registered successfully with ID: {}", users.getId());

        return new BaseResponse<>(HttpStatus.OK.value(), "Get User Profile Success", response);
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

        // invalidates old token
        tokenHelper.invalidateToken(user.getId());

        // generate new token
        String generatedToken = tokenHelper.generateToken(user);

        logger.info("Authentication success for {}", params.username());

        return new BaseResponse<>(HttpStatus.OK.value(), "Authenticate Success",
                new LoginResponse(user.getId(), generatedToken));
    }
}
