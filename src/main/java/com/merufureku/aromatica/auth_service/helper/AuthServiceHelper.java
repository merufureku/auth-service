package com.merufureku.aromatica.auth_service.helper;

import com.merufureku.aromatica.auth_service.dao.entity.UserRoles;
import com.merufureku.aromatica.auth_service.dao.entity.UserRolesPK;
import com.merufureku.aromatica.auth_service.dao.entity.Users;
import com.merufureku.aromatica.auth_service.dao.repository.RolesRepository;
import com.merufureku.aromatica.auth_service.dao.repository.UsersRepository;
import com.merufureku.aromatica.auth_service.dto.params.RegisterParam;
import com.merufureku.aromatica.auth_service.exception.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.ROLE_NOT_FOUND;
import static com.merufureku.aromatica.auth_service.enums.CustomStatusEnums.USER_HAS_NO_ROLES;

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

        var bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

        var newUser = Users.builder()
                .username(newUserParam.username())
                .password(bCryptPasswordEncoder.encode(newUserParam.password()))
                .email(newUserParam.email())
                .createdAt(LocalDateTime.now())
                .build();

        var roles = rolesRepository.findByRoleName("USER")
                .orElseThrow(() -> new ServiceException(ROLE_NOT_FOUND));

        // Create User Role
        var userRolesPK = new UserRolesPK();
        userRolesPK.setUserId(newUser.getId());
        userRolesPK.setRoleId(roles.getRoleId());

        var userRoles = new UserRoles();
        userRoles.setId(userRolesPK);
        userRoles.setUser(newUser);

        newUser.setUserRoles(userRoles);

        return usersRepository.save(newUser);
    }

    public String getUserRole(Users user){
        var userRoles = user.getUserRoles();
        if (userRoles == null ||  userRoles == null){
            throw new ServiceException(USER_HAS_NO_ROLES);
        }

        var roleId = userRoles.getId().getRoleId();
        var roles = rolesRepository.findById(roleId).orElseThrow(() -> new ServiceException(USER_HAS_NO_ROLES));

        return roles.getRoleName();
    }
}