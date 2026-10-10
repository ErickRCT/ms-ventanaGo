package com.ventanago.solicitud.repository.entity;

import com.ventanago.auth.repository.entity.Cuenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Mensaje del chat entre el cliente y el proveedor de una oferta. */
@Getter
@Setter
@Entity
@Table(name = "mensaje_oferta", indexes = @Index(name = "idx_mensaje_oferta", columnList = "oferta_id"))
public class MensajeOferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mensaje_id")
    private Long mensajeId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private Oferta oferta;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Cuenta autor;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "texto", nullable = false, length = 1000)
    private String texto;
}
