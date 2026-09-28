package com.ventanago.pauta.service.dto;

import com.ventanago.perfileria.service.dto.PerfilDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class PautaPerfilDto {

    private Long pautaPerfilId;

    private Long pautaId;

    private PerfilDto perfil;

    private String corte;

    private char orientacion;

    private Long cantidad;

    private Long variacion;

    private boolean dividir;

}
