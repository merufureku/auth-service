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
    @Column(name = "ID")
    private String id;

    @Column(name = "USER_ID", nullable = false)
    private Integer userId;

    @Column(name = "TOKEN")
    private String token;

    @Column(name = "CREATED_DT")
    private LocalDateTime createdDt;

    @Column(name = "EXPIRATION_DT")
    private LocalDateTime expirationDt;
}
