package com.merufureku.aromatica.auth_service.helper;

import com.merufureku.aromatica.auth_service.dao.entity.Token;
import com.merufureku.aromatica.auth_service.dao.entity.Users;
import com.merufureku.aromatica.auth_service.dao.repository.TokenRepository;
import com.merufureku.aromatica.auth_service.exception.ServiceException;
import com.merufureku.aromatica.auth_service.utilities.TokenUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.INVALID_TOKEN;
import static com.merufureku.aromatica.auth_service.utilities.DateUtility.isDateExpired;

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

    public String generateToken(Users user){

        var roleName = authServiceHelper.getUserRole(user);

        logger.info("Generating token for userId={}, role={}", user.getId(), roleName);

        var jti = UUID.randomUUID().toString();
        var generatedToken = tokenUtility.generateToken(jti, user.getId(), roleName);

        saveToken(jti, user.getId(), generatedToken);

        return generatedToken;
    }

    private void saveToken(String id, Integer userId, String generatedToken){

        var token = Token.builder()
                .jti(id)
                .userId(userId)
                .token(generatedToken)
                .createdDt(LocalDateTime.now())
                .expirationDt(LocalDateTime.now().plusMinutes(5))
                .build();

        tokenRepository.save(token);
    }

    public void validateToken(Integer userId, String jti,  String validatingToken){
        logger.info("Validating token for: {}", userId);

        var originalToken = tokenRepository.findByUserIdAndJti(userId, jti)
                .orElseThrow(() -> new ServiceException(INVALID_TOKEN));

        if (!originalToken.getToken().equals(validatingToken)){
            logger.info("Invalid token found!");
            throw new ServiceException(INVALID_TOKEN);
        }
        if (isDateExpired(originalToken.getExpirationDt())){
            logger.info("Token expired!");
            throw new ServiceException(INVALID_TOKEN);
        }
    }

    public void invalidateToken(Integer userId){
        logger.info("Removing token based from User ID {}", userId);
        tokenRepository.deleteById(userId);
        logger.info(TOKEN_INVALIDATED);
    }
}
