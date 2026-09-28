package com.ventanago.pauta.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipo_producto")
public class TipoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_producto_id")
    private Long tipoProductoId;

    private String nombre;

    private String descripcion;

    private String tipo_producto;

}
