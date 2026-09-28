package com.ventanago.serie.service;


import com.ventanago.serie.service.dto.SerieDto;

import java.util.List;

public interface SerieService {

    List<SerieDto> obtenerSeries();

    SerieDto buscarSerie(Long id);

    SerieDto agregarSerie(SerieDto nombre);

    SerieDto modificarSerie(SerieDto serieDto);

    boolean eliminarSerie(Long id);
}
