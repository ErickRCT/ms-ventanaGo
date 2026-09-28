package com.ventanago.vidrio.service.impl;

import com.ventanago.vidrio.repository.VidrioRepository;
import com.ventanago.vidrio.service.VidrioService;
import com.ventanago.vidrio.service.dto.VidrioDto;
import com.ventanago.vidrio.service.mapper.VidrioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VidrioServiceImpl implements VidrioService {

    private final VidrioRepository vidrioRepository;

    private final VidrioMapper vidrioMapper;

    @Override
    public List<VidrioDto> obtenerVidrios() {
        return vidrioMapper.toDtoList(vidrioRepository.findAll());
    }

    @Override
    public VidrioDto obtenerVidrio(Long vidrioId){
        if(vidrioRepository.findById(vidrioId).isPresent()){
            return vidrioMapper.toDto(vidrioRepository.findById(vidrioId).get());
        }else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public VidrioDto agregarVidrio(VidrioDto vidrioDto){
        if(vidrioDto.getVidrioId() == null) {
            return vidrioMapper.toDto(vidrioRepository.save(vidrioMapper.toEntity(vidrioDto)));
        }
        if (vidrioRepository.existsById(vidrioDto.getVidrioId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public VidrioDto modificarVidrio(VidrioDto vidrioDto){
        if(vidrioRepository.existsById(vidrioDto.getVidrioId())) {
            return vidrioMapper.toDto(vidrioRepository.save(vidrioMapper.toEntity(vidrioDto)));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public boolean eliminarVidrio(Long id){
        if (vidrioRepository.existsById(id)) {
            vidrioRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }



}
