package com.ventanago.region.service.impl;

import com.ventanago.region.repository.RegionRepository;
import com.ventanago.region.service.RegionService;
import com.ventanago.region.service.dto.RegionDto;
import com.ventanago.region.service.mapper.RegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    @Override
    public List<RegionDto> obtenerRegiones(){
        return regionMapper.toDtoList(regionRepository.findAll());
    }

    @Override
    public RegionDto buscarRegion(Long idRegion){
        if (regionRepository.findById(idRegion).isPresent()){
            return regionMapper.toDto(regionRepository.findById(idRegion).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }






}
