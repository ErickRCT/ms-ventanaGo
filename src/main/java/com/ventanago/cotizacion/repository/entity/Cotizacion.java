package com.ventanago.cotizacion.repository.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ventanago.cliente.repository.entity.Cliente;
import com.ventanago.serie.repository.entity.Serie;
import com.ventanago.ventana.repository.entity.Ventana;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "cotizacion")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cotizacion_id", nullable = false)
    private Long cotizacionId;

    @Column(name = "estado", nullable = false, length = 10)
    private String estado;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "ganancia")
    private Integer ganancia;

    @Column(name = "descuento")
    private Integer descuento;

    @Column(name = "condiciones", length = 200)
    private String condiciones;

    @Column(name = "flete", length = 200)
    private String flete;

    @Column(name = "valor_flete")
    private Integer valorFlete;

    @Column(name = "instalacion", length = 200)
    private String instalacion;

    @Column(name = "valor_instalacion")
    private Integer valorInstalacion;

    @Column(name = "otros_gastos", length = 200)
    private String otrosGastos;

    @Column(name = "valor_otros_gastos")
    private Integer valorOtrosGastos;

    @Column(name = "valor_mano_de_obra")
    private Integer valorManoDeObra;

    @Column(name = "neto")
    private Long neto;

    @Column(name = "totalm2", precision = 10, scale = 2)
    private BigDecimal totalm2;

    @Column(name = "cantidad_productos")
    private Integer cantidadProductos;

    @OneToMany(mappedBy = "cotizacion")
    @JsonManagedReference
    private Set<Ventana> ventanas;

    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "cliente_id")
    private Cliente cliente;

    @Column(name = "nombre_cotizacion")
    private String nombreCotizacion;

}