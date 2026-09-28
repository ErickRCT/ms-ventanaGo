package com.ventanago.cotizacion.service.dto;

import com.ventanago.cliente.service.dto.ClienteDto;
import com.ventanago.ventana.service.dto.VentanaDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
public class CotizacionDto {

    private Long cotizacionId;

    private String nombreCotizacion;

    private String estado;

    private LocalDate fecha;

    private Integer ganancia;

    private Integer descuento;

    private String condiciones;

    private String flete;

    private Integer valorFlete;

    private String instalacion;

    private Integer valorInstalacion;

    private String otrosGastos;

    private Integer valorOtrosGastos;

    private Integer valorManoDeObra;

    private Long neto;

    private BigDecimal totalm2;

    private Integer cantidadProductos;

    private Set<VentanaDto> ventanas;

    private ClienteDto cliente;
}
