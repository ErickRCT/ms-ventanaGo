package com.ventanago.pauta.service.dto;

import com.ventanago.serie.service.dto.SerieDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PautaDto {

    private Long pautaId;

    private String nombre;

    private String descripcion;

    private Double pesoTeoricoHorizontal;

    private Double pesoTeoricoVertical;

    private Double pesoTeoricoReforzadoHorizontal;

    private Double pesoTeoricoReforzadoVertical;

    private int verticalReforzada;

    private int horizontalReforzada;

    private Boolean isReforzada;

    private TipoPautaDto tipoPauta;

    private List<PautaVidrioDto> vidrios;

    private List<PautaQuincalleriaDto> quincallerias;

    private List<PautaPerfilDto> perfiles;

    private SerieDto serie;

}
