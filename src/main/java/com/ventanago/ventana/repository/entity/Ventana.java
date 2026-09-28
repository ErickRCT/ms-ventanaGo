package com.ventanago.ventana.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ventanago.color.repository.entity.Color;
import com.ventanago.cotizacion.repository.entity.Cotizacion;
import com.ventanago.pauta.repository.entity.Pauta;
import com.ventanago.vidrio.repository.entity.Vidrio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ventana")
public class Ventana {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ventana_id", nullable = false)
    private Long ventanaId;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "ancho", nullable = false)
    private Integer ancho;

    @Column(name = "alto", nullable = false)
    private Integer alto;

    @Column(name = "observaciones")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;

    @Column(name = "precio_neto")
    private int precioNeto;

    @ManyToOne
    @JoinColumn(name = "cotizacion_id")
    @JsonIgnore
    private Cotizacion cotizacion;

    @ManyToOne
    @JoinColumn(name = "pauta_id")
    private Pauta pauta;

    @ManyToOne
    @JoinColumn(name = "vidrio_id")
    private Vidrio vidrio;

}