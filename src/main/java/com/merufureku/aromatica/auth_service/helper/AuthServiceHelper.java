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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.*;

@Component
public class AuthServiceHelper {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;

    public AuthServiceHelper(UsersRepository usersRepository, RolesRepository rolesRepository) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
    }

    public Users saveUser(RegisterParam newUserParam){
        logger.info("Saving new user: {}", newUserParam.username());

        LocalDateTime timeNow = LocalDateTime.now();

        var bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

        var roles = rolesRepository.findByRoleName("USER")
                .orElseThrow(() -> new ServiceException(ROLE_NOT_FOUND));

        var newUser = Users.builder()
                .username(newUserParam.username())
                .password(bCryptPasswordEncoder.encode(newUserParam.password()))
                .createdAt(timeNow)
                .lastPasswordSetDt(timeNow)
                .build();

        var userDetails = UserDetails.builder()
                .user(newUser)
                .firstName(newUserParam.firstName())
                .lastName(newUserParam.lastName())
                .email(newUserParam.email())
                .createdAt(timeNow)
                .updatedAt(timeNow);

        // Create User Role
        var userRolesPK = UserRolesPK.builder()
                .userId(newUser.getId())
                .roleId(roles.getRoleId())
                .build();
        var userRoles = UserRoles.builder()
                .id(userRolesPK)
                .user(newUser)
                .build();

        newUser.setUserRoles(userRoles);
        newUser.setUserDetails(userDetails.build());

        return usersRepository.save(newUser);
    }

    public void updateLastLoginDate(Users user){
        user.setLastLogin(LocalDateTime.now());
        usersRepository.save(user);
    }

    public Users updateUser(Users user, UpdateUserDetailsParam param){

        setIfNotNull(param::firstName, user.getUserDetails()::setFirstName);
        setIfNotNull(param::lastName, user.getUserDetails()::setLastName);
        setIfNotNull(param::email, user.getUserDetails()::setEmail);
        setIfNotNull(param::phoneNumber, user.getUserDetails()::setPhoneNumber);
        setIfNotNull(param::bio, user.getUserDetails()::setBio);
        setIfNotNull(param::country, user.getUserDetails()::setCountry);
        setIfNotNull(param::profileImage, user::setProfileImage);
        setIfNotNull(param::coverPhoto, user::setCoverPhoto);

        return usersRepository.save(user);
    }

    public void validateNewPassword(ChangePasswordParam changePasswordParam){
        if (!changePasswordParam.newPassword().equals(changePasswordParam.confirmPassword())){
            logger.error("Confirm Password Mismatch");
            throw new ServiceException(CONFIRM_PASSWORD_MISMATCH);
        }

        if (changePasswordParam.oldPassword().equals(changePasswordParam.confirmPassword())){
            logger.error("Old and new password the same");
            throw new ServiceException(SAME_OLD_AND_NEW_PASSWORD);
        }
    }

    public String getUserRole(Users user){
        var userRoles = user.getUserRoles();
        if (userRoles == null){
            throw new ServiceException(USER_HAS_NO_ROLES);
        }

        var roleId = userRoles.getId().getRoleId();
        var roles = rolesRepository.findById(roleId).orElseThrow(() -> new ServiceException(USER_HAS_NO_ROLES));

        return roles.getRoleName();
    }

    private static <T> void setIfNotNull(Supplier<T> source, Consumer<T> target) {
        T value = source.get();
        if (value != null) {
            target.accept(value);
        }
    }
}