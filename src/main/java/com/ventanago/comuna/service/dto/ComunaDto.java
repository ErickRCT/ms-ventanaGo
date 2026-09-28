package com.ventanago.comuna.service.dto;

import com.ventanago.region.service.dto.RegionDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ComunaDto {

    private Long comunaId;

    private String nombre;

    private RegionDto region;

}
