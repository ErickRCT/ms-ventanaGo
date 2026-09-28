package com.ventanago.color.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ColorDto {

    private Long colorId;

    private String nombre;

    private Long valor;

}
