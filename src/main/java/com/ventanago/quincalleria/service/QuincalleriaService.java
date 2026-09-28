package com.ventanago.quincalleria.service;

import com.ventanago.quincalleria.service.dto.QuincalleriaDto;

import java.util.List;

public interface QuincalleriaService {

    List<QuincalleriaDto> obtenerQuincallerias();

    QuincalleriaDto obtenerQuincalleria(Long id);

    QuincalleriaDto agregarQuincalleria(QuincalleriaDto quincalleriaDto);

    QuincalleriaDto modificarQuincalleria(QuincalleriaDto quincalleriaDto);

    boolean eliminarQuincalleria(Long id);

    List<QuincalleriaDto> obtenerQuincalleriasPorSerieId(Long serieId);
}
