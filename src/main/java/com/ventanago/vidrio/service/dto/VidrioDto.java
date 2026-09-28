package com.ventanago.vidrio.service.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VidrioDto {

    private Long vidrioId;

    private String nombre;

    private int valor;

}
