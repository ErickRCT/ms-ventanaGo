package com.ventanago.quincalleria.service.dto;

import com.ventanago.serie.service.dto.SerieDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class QuincalleriaDto {

    private Long quincalleriaId;

    private String nombre;

    private String unidad;

    private int valor;

    private String rutaImagen;

    private SerieDto serie;

}
