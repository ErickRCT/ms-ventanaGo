package com.ventanago.ventana.service.dto;

import com.ventanago.color.service.dto.ColorDto;
import com.ventanago.pauta.service.dto.PautaDto;
import com.ventanago.vidrio.service.dto.VidrioDto;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VentanaDto {

    @Nullable
    private Long ventanaId;

    private String descripcion;

    private Integer cantidad;

    private int ancho;

    private int alto;

    private String observaciones;

    private ColorDto color;

    private int precioNeto;

    @Nullable
    private Long cotizacionId;

    private PautaDto pauta;

    private VidrioDto vidrio;
}
