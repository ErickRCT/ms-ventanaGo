package com.ventanago.notificacion.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Teléfono que recibe notificaciones push (token de Firebase Cloud Messaging) de una cuenta. */
@Getter
@Setter
@Entity
@Table(name = "dispositivo_push", indexes = @Index(name = "idx_dispositivo_push_cuenta", columnList = "cuenta_id"))
public class DispositivoPush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispositivo_push_id")
    private Long dispositivoPushId;

    @Column(name = "cuenta_id", nullable = false)
    private Long cuentaId;

    /** Un teléfono tiene un solo token: si otra cuenta entra en él, el token pasa a esa cuenta. */
    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "actualizado", nullable = false)
    private LocalDateTime actualizado;
}
