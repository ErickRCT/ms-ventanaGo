package com.ventanago.solicitud.repository.entity;

import com.ventanago.auth.repository.entity.Cuenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Nota que el cliente pone al proveedor elegido cuando el trabajo termina. Una por solicitud. */
@Getter
@Setter
@Entity
@Table(name = "valoracion")
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "valoracion_id")
    private Long valoracionId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "numero", nullable = false, unique = true)
    private Solicitud solicitud;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Cuenta proveedor;

    /** De 1 a 5. */
    @Column(name = "estrellas", nullable = false)
    private int estrellas;

    @Column(name = "comentario", length = 1000)
    private String comentario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
