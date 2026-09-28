package com.ventanago.serie.service.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerieDto {

    private Long serieId;

    private String nombre;

    private String descripcion;

}