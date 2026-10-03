package com.ventanago.auth.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Cuenta de acceso a VentanaGo. Tabla propia para no tocar la tabla "usuario" que usa el otro backend. */
@Getter
@Setter
@Entity
@Table(name = "cuenta")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cuenta_id")
    private Long cuentaId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "telefono")
    private String telefono;

    /** Solo administradores y proveedores tienen contraseña; los clientes entran sin ella. */
    @Column(name = "hash_password")
    private String hashPassword;

    /** Identificador de Google ("sub") si el cliente entró con Google. */
    @Column(name = "google_sub", unique = true)
    private String googleSub;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "creado", nullable = false)
    private LocalDateTime creado = LocalDateTime.now();
}
