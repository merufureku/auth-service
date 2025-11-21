package com.merufureku.aromatica.auth_service.dao.repository;

import com.merufureku.aromatica.auth_service.dao.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    boolean existsByUsername(String username);

    Optional<Users> findByUsername(String username);

}
