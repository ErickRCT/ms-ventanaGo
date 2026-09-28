package com.ventanago.pauta.service;

import com.ventanago.pauta.service.dto.PautaDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PautaService {
    List<PautaDto> obtenerPautas();

    @Transactional
    PautaDto obtenerPautasPorId(Long id);

    @Transactional
    List<PautaDto> obtenerPautasPorSerieId(Long idSerie);

    @Transactional
    PautaDto agregarPauta(PautaDto pautaDto);

    @Transactional
    PautaDto modificarPauta(PautaDto pautaDto);

    @Transactional
    boolean eliminarPauta(Long id);
}
