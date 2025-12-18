package com.merufureku.aromatica.auth_service.services.impl;

import com.merufureku.aromatica.auth_service.dao.entity.UserDetails;
import com.merufureku.aromatica.auth_service.dao.entity.UserRoles;
import com.merufureku.aromatica.auth_service.dao.entity.UserRolesPK;
import com.merufureku.aromatica.auth_service.dao.entity.Users;
import com.merufureku.aromatica.auth_service.dao.repository.UsersRepository;
import com.merufureku.aromatica.auth_service.dto.params.*;
import com.merufureku.aromatica.auth_service.dto.responses.*;
import com.merufureku.aromatica.auth_service.exception.ServiceException;
import com.merufureku.aromatica.auth_service.helper.AuthServiceHelper;
import com.merufureku.aromatica.auth_service.helper.TokenHelper;
import com.merufureku.aromatica.auth_service.utilities.TokenUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImpl1Test {

    @InjectMocks
    private AuthServiceImpl1 authService;

    @Mock
    private AuthServiceHelper authServiceHelper;

    @Mock
    private TokenHelper tokenHelper;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenUtility tokenUtility;

    private BaseParam baseParam;
    private Users user;
    private UserDetails userDetails;
    private RegisterParam registerParam;
    private LoginParam loginParam;
    private UpdateUserDetailsParam updateUserDetailsParam;
    private ChangePasswordParam changePasswordParam;

    @BeforeEach
    void setUp() {
        baseParam = new BaseParam(1, "testuser");

        userDetails = UserDetails.builder()
                .id(1)
                .firstName("Mark")
                .lastName("Mercado")
                .email("mark@example.com")
                .phoneNumber("+1234567890")
                .bio("Test bio")
                .country("PH]")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = Users.builder()
                .id(1)
                .username("testuser")
                .password("encoded-password")
                .createdAt(LocalDateTime.now())
                .lastPasswordSetDt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .userDetails(userDetails)
                .build();

        UserRolesPK userRolesPK = UserRolesPK.builder()
                .userId(1)
                .roleId(1)
                .build();
        UserRoles userRoles = UserRoles.builder()
                .id(userRolesPK)
                .user(user)
                .build();
        user.setUserRoles(userRoles);

        registerParam = new RegisterParam("newuser", "password123", "Jane", "Smith", "jane@example.com");
        loginParam = new LoginParam("testuser", "password123");
        updateUserDetailsParam = new UpdateUserDetailsParam(
                "Charmaine", "Yap", "Charmaine@example.com", "+1234567891", "Updated bio", "Japan", "profile.jpg", "cover.jpg"
        );
        changePasswordParam = new ChangePasswordParam("oldPassword123", "newPassword123", "newPassword123");
    }

    @Test
    void testRegister_whenUserDoesNotExist_thenRegisterSuccessfully() {
        when(usersRepository.existsByUsername(registerParam.username())).thenReturn(false);
        when(authServiceHelper.saveUser(registerParam)).thenReturn(user);

        BaseResponse<RegisterResponse> response = authService.register(registerParam, baseParam);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.status());
        assertEquals("Get User Profile Success", response.message());
        assertNotNull(response.data());
        assertEquals(user.getId(), response.data().id());

        verify(usersRepository, times(1)).existsByUsername(registerParam.username());
        verify(authServiceHelper, times(1)).saveUser(registerParam);
    }

    @Test
    void testRegister_whenUserAlreadyExists_thenThrowException() {
        when(usersRepository.existsByUsername(registerParam.username())).thenReturn(true);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.register(registerParam, baseParam));

        assertEquals(USERNAME_ALREADY_EXIST, exception.getCustomStatusEnums());
        verify(usersRepository, times(1)).existsByUsername(registerParam.username());
        verify(authServiceHelper, never()).saveUser(any(RegisterParam.class));
    }

    @Test
    void testLogin_whenCredentialsValid_thenLoginSuccessfully() {
        var loginResponse = new LoginResponse(user.getId(), new LoginResponse.Token("access-token", "refresh-token"));

        when(usersRepository.findByUsername(loginParam.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginParam.password(), user.getPassword())).thenReturn(true);
        when(tokenHelper.generateToken(user)).thenReturn(loginResponse);
        doNothing().when(tokenHelper).invalidateAllUserToken(user.getId());
        doNothing().when(authServiceHelper).updateLastLoginDate(user);

        BaseResponse<LoginResponse> response = authService.login(loginParam, baseParam);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.status());
        assertEquals("Authenticate Success", response.message());
        assertNotNull(response.data());
        assertEquals(user.getId(), response.data().id());

        verify(usersRepository, times(1)).findByUsername(loginParam.username());
        verify(passwordEncoder, times(1)).matches(loginParam.password(), user.getPassword());
        verify(tokenHelper, times(1)).invalidateAllUserToken(user.getId());
        verify(tokenHelper, times(1)).generateToken(user);
        verify(authServiceHelper, times(1)).updateLastLoginDate(user);
    }

    @Test
    void testLogin_whenUserNotFound_thenThrowException() {
        when(usersRepository.findByUsername(loginParam.username())).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.login(loginParam, baseParam));

        assertEquals(NO_USER_FOUND, exception.getCustomStatusEnums());
        verify(usersRepository, times(1)).findByUsername(loginParam.username());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_whenPasswordInvalid_thenThrowException() {
        when(usersRepository.findByUsername(loginParam.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginParam.password(), user.getPassword())).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.login(loginParam, baseParam));

        assertEquals(NO_USER_FOUND, exception.getCustomStatusEnums());
        verify(usersRepository, times(1)).findByUsername(loginParam.username());
        verify(passwordEncoder, times(1)).matches(loginParam.password(), user.getPassword());
        verify(tokenHelper, never()).invalidateAllUserToken(anyInt());
    }

    @Test
    void testLogout_thenInvalidateTokenSuccessfully() {
        doNothing().when(tokenHelper).invalidateAllUserToken(user.getId());

        boolean result = authService.logout(user.getId(), baseParam);

        assertTrue(result);
        verify(tokenHelper, times(1)).invalidateAllUserToken(user.getId());
    }

    @Test
    void testMyDetails_whenUserExists_thenReturnUserDetails() {
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));

        BaseResponse<MyDetailsResponse> response = authService.myDetails(user.getId(), baseParam);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.status());
        assertEquals("Get User Details Success", response.message());
        assertNotNull(response.data());

        verify(usersRepository, times(1)).findById(user.getId());
    }

    @Test
    void testMyDetails_whenUserNotExists_thenThrowException() {
        when(usersRepository.findById(user.getId())).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.myDetails(user.getId(), baseParam));

        assertEquals(NO_USER_FOUND, exception.getCustomStatusEnums());
        verify(usersRepository, times(1)).findById(user.getId());
    }

    @Test
    void testUpdateProfile_whenUserExists_thenUpdateSuccessfully() {
        var updatedUser = Users.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .createdAt(user.getCreatedAt())
                .userDetails(userDetails)
                .build();

        when(usersRepository.findByIdWithUserDetails(user.getId())).thenReturn(Optional.of(user));
        when(authServiceHelper.updateUser(user, updateUserDetailsParam)).thenReturn(updatedUser);

        BaseResponse<UpdateUserDetailsResponse> response = authService.updateProfile(user.getId(), updateUserDetailsParam, baseParam);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.status());
        assertEquals("Update Profile Success", response.message());
        assertNotNull(response.data());

        verify(usersRepository, times(1)).findByIdWithUserDetails(user.getId());
        verify(authServiceHelper, times(1)).updateUser(user, updateUserDetailsParam);
    }

    @Test
    void testUpdateProfile_whenUserNotExists_thenThrowException() {
        when(usersRepository.findByIdWithUserDetails(user.getId())).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.updateProfile(user.getId(), updateUserDetailsParam, baseParam));

        assertEquals(NO_USER_FOUND, exception.getCustomStatusEnums());
        verify(usersRepository, times(1)).findByIdWithUserDetails(user.getId());
        verify(authServiceHelper, never()).updateUser(any(Users.class), any(UpdateUserDetailsParam.class));
    }

    @Test
    void testDeleteAccount_whenUserExists_thenDeleteSuccessfully() {
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(usersRepository).delete(user);

        boolean result = authService.deleteAccount(user.getId(), baseParam);

        assertTrue(result);
        verify(usersRepository, times(1)).findById(user.getId());
        verify(usersRepository, times(1)).delete(user);
    }

    @Test
    void testDeleteAccount_whenUserNotExists_thenThrowException() {
        when(usersRepository.findById(user.getId())).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.deleteAccount(user.getId(), baseParam));

        assertEquals(NO_USER_FOUND, exception.getCustomStatusEnums());
        verify(usersRepository, times(1)).findById(user.getId());
        verify(usersRepository, never()).delete(any(Users.class));
    }

    @Test
    void testChangePassword_whenValid_thenChangeSuccessfully() {
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(authServiceHelper).validateNewPassword(changePasswordParam);
        when(passwordEncoder.matches(changePasswordParam.oldPassword(), user.getPassword())).thenReturn(true);
        when(usersRepository.save(any(Users.class))).thenReturn(user);

        boolean result = authService.changePassword(user.getId(), changePasswordParam, baseParam);

        assertTrue(result);
        verify(authServiceHelper, atMost(1)).validateNewPassword(changePasswordParam);
        verify(usersRepository, atMost(1)).findById(user.getId());
        verify(passwordEncoder, atMost(1)).matches(changePasswordParam.oldPassword(), user.getPassword());
        verify(usersRepository, atMost(1)).save(any(Users.class));
    }

    @Test
    void testChangePassword_whenNewPasswordValidationFails_thenThrowException() {
        var invalidChangePasswordParam = new ChangePasswordParam("oldPassword123", "newPassword123", "differentPassword");

        doThrow(new ServiceException(CONFIRM_PASSWORD_MISMATCH))
                .when(authServiceHelper).validateNewPassword(invalidChangePasswordParam);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.changePassword(user.getId(), invalidChangePasswordParam, baseParam));

        assertEquals(CONFIRM_PASSWORD_MISMATCH, exception.getCustomStatusEnums());
        verify(authServiceHelper, times(1)).validateNewPassword(invalidChangePasswordParam);
    }

    @Test
    void testChangePassword_whenUserNotExists_thenThrowException() {
        when(usersRepository.findById(user.getId())).thenReturn(Optional.empty());
        doNothing().when(authServiceHelper).validateNewPassword(changePasswordParam);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.changePassword(user.getId(), changePasswordParam, baseParam));

        assertEquals(NO_USER_FOUND, exception.getCustomStatusEnums());
        verify(authServiceHelper, times(1)).validateNewPassword(changePasswordParam);
        verify(usersRepository, times(1)).findById(user.getId());
    }

    @Test
    void testChangePassword_whenOldPasswordInvalid_thenThrowException() {
        doNothing().when(authServiceHelper).validateNewPassword(changePasswordParam);
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changePasswordParam.oldPassword(), user.getPassword())).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.changePassword(user.getId(), changePasswordParam, baseParam));

        assertEquals(INVALID_PASSWORD, exception.getCustomStatusEnums());
        verify(authServiceHelper, times(1)).validateNewPassword(changePasswordParam);
        verify(usersRepository, times(1)).findById(user.getId());
        verify(passwordEncoder, times(1)).matches(changePasswordParam.oldPassword(), user.getPassword());
    }

    @Test
    void testRefreshAccessToken_whenValidToken_thenReturnNewAccessToken() {
        var refreshToken = "Bearer refresh-token-value";
        var claims = mock(io.jsonwebtoken.Claims.class);
        var newAccessToken = "new-access-token-value";

        when(tokenUtility.parseToken(anyString(), anyString())).thenReturn(claims);
        when(claims.getId()).thenReturn("test-jti");
        when(claims.get("userId", Integer.class)).thenReturn(user.getId());
        doNothing().when(tokenHelper).validateToken(anyInt(), anyString(), anyString(), anyString());
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenHelper.generateNewAccessToken(user)).thenReturn(newAccessToken);

        BaseResponse<NewAccessTokenResponse> response = authService.refreshAccessToken(refreshToken, baseParam);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.status());

        verify(usersRepository, atMost(1)).findById(user.getId());
    }

    @Test
    void testRefreshAccessToken_whenUserNotFound_thenThrowException() {
        var refreshToken = "Bearer refresh-token-value";
        var claims = mock(io.jsonwebtoken.Claims.class);

        when(tokenUtility.parseToken(anyString(), anyString())).thenReturn(claims);
        when(claims.getId()).thenReturn("test-jti");
        when(claims.get("userId", Integer.class)).thenReturn(user.getId());
        when(usersRepository.findById(user.getId())).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.refreshAccessToken(refreshToken, baseParam));

        assertEquals(NO_USER_FOUND, exception.getCustomStatusEnums());
    }
}

