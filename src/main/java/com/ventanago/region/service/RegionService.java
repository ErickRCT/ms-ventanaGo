package com.ventanago.region.service;

import com.ventanago.region.service.dto.RegionDto;

import java.util.List;

public interface RegionService {
    List<RegionDto> obtenerRegiones();

    RegionDto buscarRegion(Long idRegion);
}
