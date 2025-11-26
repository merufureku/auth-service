package com.merufureku.aromatica.auth_service.helper;

import com.merufureku.aromatica.auth_service.dao.entity.Token;
import com.merufureku.aromatica.auth_service.dao.entity.Users;
import com.merufureku.aromatica.auth_service.dao.repository.TokenRepository;
import com.merufureku.aromatica.auth_service.dto.responses.LoginResponse;
import com.merufureku.aromatica.auth_service.exception.ServiceException;
import com.merufureku.aromatica.auth_service.utilities.TokenUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.merufureku.aromatica.auth_service.constants.AuthConstants.*;
import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.INVALID_TOKEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenHelperTest {

    @InjectMocks
    private TokenHelper tokenHelper;

    @Mock
    private TokenUtility tokenUtility;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private AuthServiceHelper authServiceHelper;

    private Users user;
    private Token token;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1)
                .username("testuser")
                .password("password")
                .createdAt(LocalDateTime.now())
                .build();

        token = Token.builder()
                .userId(1)
                .token("test-token-value")
                .type(ACCESS_TOKEN)
                .jti("test-jti")
                .createdDt(LocalDateTime.now())
                .expirationDt(LocalDateTime.now().plusMinutes(ACCESS_TOKEN_EXPIRATION_MINUTES))
                .build();
    }

    @Test
    void testGenerateToken_thenReturnLoginResponse() {
        when(authServiceHelper.getUserRole(user)).thenReturn("USER");
        when(tokenUtility.generateToken(anyString(), eq(user.getId()), eq(ACCESS_TOKEN), eq("USER")))
                .thenReturn("access-token-value");
        when(tokenUtility.generateToken(anyString(), eq(user.getId()), eq(REFRESH_TOKEN), eq("USER")))
                .thenReturn("refresh-token-value");
        when(tokenRepository.save(any(Token.class))).thenReturn(token);

        LoginResponse response = tokenHelper.generateToken(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.id());
        assertNotNull(response.token());
        assertEquals("access-token-value", response.token().accessToken());
        assertEquals("refresh-token-value", response.token().refreshToken());

        verify(authServiceHelper, times(1)).getUserRole(user);
        verify(tokenUtility, times(2)).generateToken(anyString(), eq(user.getId()), anyString(), eq("USER"));
        verify(tokenRepository, times(2)).save(any(Token.class));
    }

    @Test
    void testGenerateNewAccessToken_thenReturnNewToken() {
        when(authServiceHelper.getUserRole(user)).thenReturn("USER");
        when(tokenUtility.generateToken(anyString(), eq(user.getId()), eq(ACCESS_TOKEN), eq("USER")))
                .thenReturn("new-access-token");
        doNothing().when(tokenRepository).deleteByUserIdAndType(user.getId(), ACCESS_TOKEN);
        when(tokenRepository.save(any(Token.class))).thenReturn(token);

        String result = tokenHelper.generateNewAccessToken(user);

        assertNotNull(result);
        assertEquals("new-access-token", result);

        verify(tokenRepository, times(1)).deleteByUserIdAndType(user.getId(), ACCESS_TOKEN);
        verify(tokenUtility, times(1)).generateToken(anyString(), eq(user.getId()), eq(ACCESS_TOKEN), eq("USER"));
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    void testValidateToken_whenValid_thenPassValidation() {
        token.setExpirationDt(LocalDateTime.now().plusMinutes(10));
        when(tokenRepository.findByUserIdAndJtiAndType(1, "test-jti", ACCESS_TOKEN))
                .thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> tokenHelper.validateToken(1, "test-jti", token.getToken(), ACCESS_TOKEN));

        verify(tokenRepository, times(1)).findByUserIdAndJtiAndType(1, "test-jti", ACCESS_TOKEN);
    }

    @Test
    void testValidateToken_whenTokenNotFound_thenThrowException() {
        when(tokenRepository.findByUserIdAndJtiAndType(1, "test-jti", ACCESS_TOKEN))
                .thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> tokenHelper.validateToken(1, "test-jti", "token-value", ACCESS_TOKEN));

        assertEquals(INVALID_TOKEN, exception.getCustomStatusEnums());
        verify(tokenRepository, times(1)).findByUserIdAndJtiAndType(1, "test-jti", ACCESS_TOKEN);
    }

    @Test
    void testValidateToken_whenTokenMismatch_thenThrowException() {
        when(tokenRepository.findByUserIdAndJtiAndType(1, "test-jti", ACCESS_TOKEN))
                .thenReturn(Optional.of(token));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> tokenHelper.validateToken(1, "test-jti", "wrong-token-value", ACCESS_TOKEN));

        assertEquals(INVALID_TOKEN, exception.getCustomStatusEnums());
        verify(tokenRepository, times(1)).findByUserIdAndJtiAndType(1, "test-jti", ACCESS_TOKEN);
    }

    @Test
    void testInvalidateAllUserToken_thenDeleteAllTokens() {
        doNothing().when(tokenRepository).deleteByUserId(user.getId());

        tokenHelper.invalidateAllUserToken(user.getId());

        verify(tokenRepository, times(1)).deleteByUserId(user.getId());
    }
}

