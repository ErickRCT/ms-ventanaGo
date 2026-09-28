package com.ventanago.login.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario", nullable = false)
    private String usuario;

    @Column(name = "hash_password")
    private String hashPassword;

    @Column(name = "roles")
    private String roles;

}