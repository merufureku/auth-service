package com.merufureku.aromatica.auth_service.helper;

import com.merufureku.aromatica.auth_service.dao.entity.Token;
import com.merufureku.aromatica.auth_service.dao.entity.Users;
import com.merufureku.aromatica.auth_service.dao.repository.TokenRepository;
import com.merufureku.aromatica.auth_service.dto.responses.LoginResponse;
import com.merufureku.aromatica.auth_service.exception.ServiceException;
import com.merufureku.aromatica.auth_service.utilities.TokenUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.merufureku.aromatica.auth_service.constants.AuthConstants.*;
import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.INVALID_TOKEN;
import static com.merufureku.aromatica.auth_service.utilities.DateUtility.isAccessTokenExpired;

@Component
public class TokenHelper {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String TOKEN_INVALIDATED = "Token Invalidated!";

    private final TokenUtility tokenUtility;
    private final TokenRepository tokenRepository;
    private final AuthServiceHelper authServiceHelper;

    public TokenHelper(TokenUtility tokenUtility, TokenRepository tokenRepository, AuthServiceHelper authServiceHelper) {
        this.tokenUtility = tokenUtility;
        this.tokenRepository = tokenRepository;
        this.authServiceHelper = authServiceHelper;
    }

    public LoginResponse generateToken(Users user){

        var roleName = authServiceHelper.getUserRole(user);

        logger.info("Generating token for userId={}, role={}", user.getId(), roleName);

        var accessJti = UUID.randomUUID().toString();
        var refreshJti = UUID.randomUUID().toString();
        var generatedAccessToken = tokenUtility.generateToken(accessJti, user.getId(), ACCESS_TOKEN, roleName);
        var generatedRefreshToken = tokenUtility.generateToken(refreshJti, user.getId(), REFRESH_TOKEN, roleName);

        saveToken(user.getId(), ACCESS_TOKEN, accessJti, generatedAccessToken);
        saveToken(user.getId(), REFRESH_TOKEN, refreshJti, generatedRefreshToken);

        return new LoginResponse(user.getId(), new LoginResponse
                .Token(generatedAccessToken, generatedRefreshToken));
    }

    private void saveToken(Integer userId, String type, String jti, String generatedToken){
        var timeNow = LocalDateTime.now();
        var expirationDate = type.equals(ACCESS_TOKEN) ?
                timeNow.plusMinutes(ACCESS_TOKEN_EXPIRATION_MINUTES) :
                timeNow.plusDays(REFRESH_TOKEN_EXPIRATION_DAYS);

        var token = Token.builder()
                .userId(userId)
                .token(generatedToken)
                .type(type)
                .jti(jti)
                .createdDt(timeNow)
                .expirationDt(expirationDate)
                .build();

        tokenRepository.save(token);
    }

    public String generateNewAccessToken(Users user){
        tokenRepository.deleteByUserIdAndType(user.getId(), ACCESS_TOKEN);

        var accessJti = UUID.randomUUID().toString();
        var roleName = authServiceHelper.getUserRole(user);
        var token = tokenUtility.generateToken(accessJti, user.getId(), ACCESS_TOKEN, roleName);

        saveToken(user.getId(), ACCESS_TOKEN, accessJti, token);

        return token;
    }

    public void validateToken(Integer userId, String jti, String validatingToken, String tokenType){
        logger.info("Validating token for: {}", userId);

        var originalToken = tokenRepository.findByUserIdAndJtiAndType(userId, jti, tokenType)
                .orElseThrow(() -> new ServiceException(INVALID_TOKEN));

        if (!originalToken.getToken().equals(validatingToken)){
            logger.info("Invalid token found!");
            throw new ServiceException(INVALID_TOKEN);
        }
        if (isAccessTokenExpired(originalToken.getExpirationDt(), tokenType)){
            logger.info("Token expired!");
            throw new ServiceException(INVALID_TOKEN);
        }
    }

    public void invalidateAllUserToken(Integer userId){
        logger.info("Removing refresh and access token of User ID {}", userId);
        tokenRepository.deleteByUserId(userId);
        logger.info(TOKEN_INVALIDATED);
    }
}
