package com.merufureku.aromatica.auth_service.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "user_roles")
@AllArgsConstructor
@NoArgsConstructor
public class UserRoles {

    @EmbeddedId
    private UserRolesPK id;

    @OneToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Users user;
}
