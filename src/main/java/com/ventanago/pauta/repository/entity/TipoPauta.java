package com.ventanago.pauta.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipo_pauta")
public class TipoPauta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_pauta_id")
    private Long tipoPautaId;

    @Column
    private String nombre;

    @Column(name = "ruta_imagen")
    private String rutaImagen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_producto_id", referencedColumnName = "tipo_producto_id")
    private TipoProducto tipoProducto;





}
