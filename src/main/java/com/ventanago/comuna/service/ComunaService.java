package com.ventanago.comuna.service;

import com.ventanago.comuna.service.dto.ComunaDto;

import java.util.List;

public interface ComunaService {

    List<ComunaDto> comunas();

    List<ComunaDto> comunasPorRegion(Long regionId);
}
