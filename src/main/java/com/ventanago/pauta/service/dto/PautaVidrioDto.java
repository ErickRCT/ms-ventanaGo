package com.ventanago.pauta.service.dto;

import com.ventanago.vidrio.service.dto.VidrioDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PautaVidrioDto {

    private Long pautaVidrioId;

    private Long pautaId;

    private Long cantidad;

    private Long variacionH;

    private Long variacionV;

    private String formula;

    private VidrioDto vidrio;

    private String nombre;

    private Long valor;



}
