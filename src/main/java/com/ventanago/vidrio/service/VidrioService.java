package com.ventanago.vidrio.service;

import com.ventanago.vidrio.service.dto.VidrioDto;

import java.util.List;

public interface VidrioService {
    List<VidrioDto> obtenerVidrios();

    VidrioDto obtenerVidrio(Long vidrioId);

    VidrioDto agregarVidrio(VidrioDto vidrioDto);

    VidrioDto modificarVidrio(VidrioDto vidrioDto);

    boolean eliminarVidrio(Long id);
}
