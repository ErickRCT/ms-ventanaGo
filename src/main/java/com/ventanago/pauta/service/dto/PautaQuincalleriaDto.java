package com.ventanago.pauta.service.dto;

import com.ventanago.quincalleria.service.dto.QuincalleriaDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PautaQuincalleriaDto {

    private Long pautaQuincalleriaId;

    private Long pautaId;

    private QuincalleriaDto quincalleria;

    private int cantidad;

    private int variacionH;

    private int variacionV;
}
