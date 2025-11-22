package com.merufureku.aromatica.auth_service.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "token")
@Builder
public class Token {

    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "jti")
    private String jti;

    @Column(name = "token")
    private String token;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @Column(name = "expiration_dt")
    private LocalDateTime expirationDt;
}
