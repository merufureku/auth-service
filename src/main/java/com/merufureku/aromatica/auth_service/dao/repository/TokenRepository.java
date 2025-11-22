package com.merufureku.aromatica.auth_service.dao.repository;

import com.merufureku.aromatica.auth_service.dao.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByUserIdAndJti(Integer userId, String token);

}
