package com.ventanago.pauta.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TipoProductoDto {

    private Long tipoProductoId;

    private String nombre;

    private String descripcion;

    private String tipoProducto;
}
