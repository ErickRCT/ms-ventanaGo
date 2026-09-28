package com.ventanago.perfileria.service.dto;

import com.ventanago.serie.service.dto.SerieDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerfilDto {

    private Long perfilId;

    private String codigo;

    private String descripcion;

    private String peso;

    private Boolean isBastidor;

    private TipoPerfilDto tipoPerfil;

    private SerieDto serie;

    private boolean reforzado;

    private String rutaImagen;

    private String orientacion;

}
