package com.ventanago.solicitud.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Ventana de una oferta: copia de la pedida, con el precio del proveedor y sus medidas si las cambió. */
@Getter
@Setter
@Entity
@Table(name = "oferta_item")
public class OfertaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oferta_item_id")
    private Long ofertaItemId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private Oferta oferta;

    /** Ventana de la solicitud a la que corresponde. */
    @Column(name = "solicitud_item_id", nullable = false)
    private Long solicitudItemId;

    @Embedded
    private DatosVentana ventana = new DatosVentana();

    @Column(name = "precio_unitario", nullable = false)
    private Long precioUnitario;
}
