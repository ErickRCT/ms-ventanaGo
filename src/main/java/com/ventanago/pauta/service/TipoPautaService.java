package com.ventanago.pauta.service;

import com.ventanago.pauta.service.dto.TipoPautaDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TipoPautaService {
    @Transactional
    List<TipoPautaDto> obtenerTipoPautas();

    @Transactional
    TipoPautaDto agregarTipoPauta(TipoPautaDto tipoPautaDto);
}
