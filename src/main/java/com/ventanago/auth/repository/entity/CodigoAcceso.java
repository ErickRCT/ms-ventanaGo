package com.ventanago.auth.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Código de un solo uso enviado al correo del cliente para entrar sin contraseña. */
@Getter
@Setter
@Entity
@Table(name = "codigo_acceso", indexes = @Index(name = "idx_codigo_acceso_email", columnList = "email"))
public class CodigoAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_acceso_id")
    private Long codigoAccesoId;

    @Column(name = "email", nullable = false)
    private String email;

    /** Se guarda el hash, nunca el código. */
    @Column(name = "hash_codigo", nullable = false)
    private String hashCodigo;

    @Column(name = "creado", nullable = false)
    private LocalDateTime creado;

    @Column(name = "expira", nullable = false)
    private LocalDateTime expira;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "usado", nullable = false)
    private boolean usado;
}
