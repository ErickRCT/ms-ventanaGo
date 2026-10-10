package com.ventanago.solicitud.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Foto del lugar que el cliente adjunta a su solicitud (ya comprimida por la app). */
@Getter
@Setter
@Entity
@Table(name = "solicitud_foto")
public class FotoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "foto_id")
    private Long fotoId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "numero", nullable = false)
    private Solicitud solicitud;

    @Column(name = "tipo_contenido", nullable = false, length = 30)
    private String tipoContenido;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "datos", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] datos;
}
