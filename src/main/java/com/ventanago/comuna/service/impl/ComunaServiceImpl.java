package com.ventanago.comuna.service.impl;

import com.ventanago.comuna.repository.ComunaRepository;
import com.ventanago.comuna.service.ComunaService;
import com.ventanago.comuna.service.dto.ComunaDto;
import com.ventanago.comuna.service.mapper.ComunaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComunaServiceImpl implements ComunaService {

    private final ComunaRepository comunaRepository;

    private final ComunaMapper comunaMapper;

    @Override
    public List<ComunaDto> comunas(){
        return comunaMapper.toDtoList(comunaRepository.findAll());
    }

    @Override
    public List<ComunaDto> comunasPorRegion(Long regionId){
        return comunaMapper.toDtoList(comunaRepository.findByRegion_RegionId(regionId));
    }

}
