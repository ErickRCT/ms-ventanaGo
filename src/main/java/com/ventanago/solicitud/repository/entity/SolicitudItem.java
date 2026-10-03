package com.ventanago.solicitud.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "solicitud_item")
public class SolicitudItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solicitud_item_id")
    private Long solicitudItemId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "numero", nullable = false)
    private Solicitud solicitud;

    /** true: copia de lo que pidió el cliente, guardada cuando el proveedor modifica la solicitud. */
    @Column(name = "original", nullable = false)
    private boolean original;

    @Embedded
    private DatosVentana ventana = new DatosVentana();

    /** Lo define el proveedor al responder. */
    @Column(name = "precio_unitario")
    private Long precioUnitario;
}
