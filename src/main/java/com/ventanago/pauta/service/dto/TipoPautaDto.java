package com.ventanago.pauta.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TipoPautaDto {

    private Long tipoPautaId;

    private String nombre;

    private String rutaImagen;

    private TipoProductoDto tipoProducto;

}
