package com.merufureku.aromatica.auth_service.dto.responses;

import com.merufureku.aromatica.auth_service.dao.entity.Users;

public record MyDetailsResponse(Integer id, String email, String firstName, String lastName, int roleId) {

    public MyDetailsResponse(Users user) {
        this(
            user.getId(),
            user.getUserDetails().getEmail(),
            user.getUserDetails().getFirstName(),
            user.getUserDetails().getLastName(),
            user.getUserRoles().getId().getRoleId()
        );
    }

}
