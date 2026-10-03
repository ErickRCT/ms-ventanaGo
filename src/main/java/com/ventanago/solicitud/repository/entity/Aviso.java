package com.ventanago.solicitud.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Notificación dentro de la app (campana del encabezado). */
@Getter
@Setter
@Entity
@Table(name = "aviso", indexes = @Index(name = "idx_aviso_destinatario", columnList = "destinatario"))
public class Aviso {

    /** Avisos para todos los proveedores (solicitudes nuevas), mientras no se asignen a uno en particular. */
    public static final String PROVEEDORES = "PROVEEDORES";

    public static String deCuenta(Long cuentaId) {
        return "CUENTA:" + cuentaId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aviso_id")
    private Long avisoId;

    /** "PROVEEDORES" o "CUENTA:<id>". */
    @Column(name = "destinatario", nullable = false, length = 40)
    private String destinatario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "solicitud_numero", nullable = false)
    private Long solicitudNumero;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "mensaje", length = 2000)
    private String mensaje;

    @Column(name = "leido", nullable = false)
    private boolean leido;
}
