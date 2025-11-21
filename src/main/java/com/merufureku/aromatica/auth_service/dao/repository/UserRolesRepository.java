package com.merufureku.aromatica.auth_service.dao.repository;

import com.merufureku.aromatica.auth_service.dao.entity.UserRoles;
import com.merufureku.aromatica.auth_service.dao.entity.UserRolesPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRolesRepository extends JpaRepository<UserRoles, UserRolesPK> {
}
