package com.ventanago.solicitud.repository.entity;

import com.ventanago.auth.repository.entity.Cuenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Ventana en el carrito de un cliente, antes de enviar la solicitud. */
@Getter
@Setter
@Entity
@Table(name = "carrito_item")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrito_item_id")
    private Long carritoItemId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Embedded
    private DatosVentana ventana = new DatosVentana();
}
