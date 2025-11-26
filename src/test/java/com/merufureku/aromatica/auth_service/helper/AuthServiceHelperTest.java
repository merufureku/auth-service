package com.merufureku.aromatica.auth_service.helper;

import com.merufureku.aromatica.auth_service.dao.entity.UserDetails;
import com.merufureku.aromatica.auth_service.dao.entity.UserRoles;
import com.merufureku.aromatica.auth_service.dao.entity.UserRolesPK;
import com.merufureku.aromatica.auth_service.dao.entity.Users;
import com.merufureku.aromatica.auth_service.dao.repository.RolesRepository;
import com.merufureku.aromatica.auth_service.dao.repository.UsersRepository;
import com.merufureku.aromatica.auth_service.dto.params.ChangePasswordParam;
import com.merufureku.aromatica.auth_service.dto.params.RegisterParam;
import com.merufureku.aromatica.auth_service.dto.params.UpdateUserDetailsParam;
import com.merufureku.aromatica.auth_service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.merufureku.aromatica.auth_service.dao.entity.Roles;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceHelperTest {

    @InjectMocks
    private AuthServiceHelper authServiceHelper;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private RolesRepository rolesRepository;

    private RegisterParam registerParam;
    private Users user;
    private UserDetails userDetails;
    private Roles role;
    private UpdateUserDetailsParam updateUserDetailsParam;

    @BeforeEach
    void setUp() {
        registerParam = new RegisterParam("testuser", "password123", "John", "Doe", "john@example.com");

        userDetails = UserDetails.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("+1234567890")
                .bio("Test bio")
                .country("USA")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = Users.builder()
                .id(1)
                .username("testuser")
                .password("encoded-password")
                .createdAt(LocalDateTime.now())
                .lastPasswordSetDt(LocalDateTime.now())
                .userDetails(userDetails)
                .build();

        role = new Roles();
        role.setRoleId(1);
        role.setRoleName("USER");

        updateUserDetailsParam = new UpdateUserDetailsParam(
                "Jane", "Smith", "jane@example.com", "+1234567891", "Updated bio", "Canada", "profile.jpg", "cover.jpg"
        );
    }

    @Test
    void testSaveUser_whenValid_thenSaveUserSuccessfully() {
        when(rolesRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(usersRepository.save(any(Users.class))).thenReturn(user);

        Users result = authServiceHelper.saveUser(registerParam);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertNotNull(result.getUserDetails());
        assertEquals("john@example.com", result.getUserDetails().getEmail());

        verify(rolesRepository, times(1)).findByRoleName("USER");
        verify(usersRepository, times(1)).save(any(Users.class));
    }

    @Test
    void testSaveUser_whenRoleNotFound_thenThrowException() {
        when(rolesRepository.findByRoleName("USER")).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authServiceHelper.saveUser(registerParam));

        assertEquals(ROLE_NOT_FOUND, exception.getCustomStatusEnums());
        verify(rolesRepository, times(1)).findByRoleName("USER");
        verify(usersRepository, never()).save(any(Users.class));
    }

    @Test
    void testUpdateLastLoginDate_whenValid_thenUpdateSuccessfully() {
        when(usersRepository.save(user)).thenReturn(user);

        authServiceHelper.updateLastLoginDate(user);

        assertNotNull(user.getLastLogin());
        verify(usersRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUser_whenValid_thenUpdateSuccessfully() {
        when(usersRepository.save(user)).thenReturn(user);

        Users result = authServiceHelper.updateUser(user, updateUserDetailsParam);

        assertNotNull(result);
        assertEquals("Jane", result.getUserDetails().getFirstName());
        assertEquals("jane@example.com", result.getUserDetails().getEmail());

        verify(usersRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUser_whenFieldsPartiallyUpdated_thenUpdateOnlyProvidedFields() {
        var partialUpdateParam = new UpdateUserDetailsParam(
                "Jane", null, null, null, null, null, null, null
        );

        when(usersRepository.save(user)).thenReturn(user);

        Users result = authServiceHelper.updateUser(user, partialUpdateParam);

        assertNotNull(result);
        assertEquals("Jane", result.getUserDetails().getFirstName());

        verify(usersRepository, times(1)).save(user);
    }

    @Test
    void testValidateNewPassword_whenPasswordsMatch_thenPassValidation() {
        var validChangePasswordParam = new ChangePasswordParam("oldPassword123", "newPassword123", "newPassword123");

        assertDoesNotThrow(() -> authServiceHelper.validateNewPassword(validChangePasswordParam));
    }

    @Test
    void testValidateNewPassword_whenConfirmPasswordMismatch_thenThrowException() {
        var invalidChangePasswordParam = new ChangePasswordParam("oldPassword123", "newPassword123", "differentPassword");

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authServiceHelper.validateNewPassword(invalidChangePasswordParam));

        assertEquals(CONFIRM_PASSWORD_MISMATCH, exception.getCustomStatusEnums());
    }

    @Test
    void testValidateNewPassword_whenOldAndNewPasswordSame_thenThrowException() {
        var samePasswordParam = new ChangePasswordParam("password123", "password123", "password123");

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authServiceHelper.validateNewPassword(samePasswordParam));

        assertEquals(SAME_OLD_AND_NEW_PASSWORD, exception.getCustomStatusEnums());
    }

    @Test
    void testGetUserRole_whenUserHasRole_thenReturnRoleName() {
        UserRolesPK userRolesPK = UserRolesPK.builder()
                .userId(1)
                .roleId(1)
                .build();

        UserRoles userRoles = UserRoles.builder()
                .id(userRolesPK)
                .user(user)
                .build();

        user.setUserRoles(userRoles);

        when(rolesRepository.findById(1)).thenReturn(Optional.of(role));

        String result = authServiceHelper.getUserRole(user);

        assertNotNull(result);
        assertEquals("USER", result);

        verify(rolesRepository, times(1)).findById(1);
    }

    @Test
    void testGetUserRole_whenUserHasNoRole_thenThrowException() {
        user.setUserRoles(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authServiceHelper.getUserRole(user));

        assertEquals(USER_HAS_NO_ROLES, exception.getCustomStatusEnums());
    }

    @Test
    void testGetUserRole_whenRoleNotFound_thenThrowException() {
        UserRolesPK userRolesPK = UserRolesPK.builder()
                .userId(1)
                .roleId(1)
                .build();

        UserRoles userRoles = UserRoles.builder()
                .id(userRolesPK)
                .user(user)
                .build();

        user.setUserRoles(userRoles);

        when(rolesRepository.findById(1)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> authServiceHelper.getUserRole(user));

        assertEquals(USER_HAS_NO_ROLES, exception.getCustomStatusEnums());
        verify(rolesRepository, times(1)).findById(1);
    }
}

