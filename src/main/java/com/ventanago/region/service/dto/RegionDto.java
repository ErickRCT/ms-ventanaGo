package com.ventanago.region.service.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegionDto {

    private Long regionId;

    private String nombre;

    private String codigo;

}
