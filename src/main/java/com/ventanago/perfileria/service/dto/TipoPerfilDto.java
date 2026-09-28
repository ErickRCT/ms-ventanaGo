package com.ventanago.perfileria.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TipoPerfilDto {

    private Long tipoPerfilId;

    private String nombre;

}
