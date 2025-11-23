package com.merufureku.aromatica.auth_service.dao.entity;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "token")
    private String token;

    @Column(name = "type")
    private String type;

    @Column(name = "jti")
    private String jti;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @Column(name = "expiration_dt")
    private LocalDateTime expirationDt;
}
