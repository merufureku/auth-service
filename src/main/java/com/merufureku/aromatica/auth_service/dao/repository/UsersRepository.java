package com.merufureku.aromatica.auth_service.dao.repository;

import com.merufureku.aromatica.auth_service.dao.entity.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"userDetails"})
    @Query("SELECT u FROM Users u WHERE u.id = :id")
    Optional<Users> findByIdWithUserDetails(@Param("id") Integer id);

    Optional<Users> findByUsername(String username);

}
