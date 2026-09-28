package com.ventanago.serie.service.impl;


import com.ventanago.serie.repository.SerieRepository;
import com.ventanago.serie.service.SerieService;
import com.ventanago.serie.service.dto.SerieDto;
import com.ventanago.serie.service.mapper.SerieMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SerieServiceImpl implements SerieService {

    private final SerieRepository serieRepository;
    private final SerieMapper serieMapper;

    @Override
    public List<SerieDto> obtenerSeries() {
        return serieMapper.toDtoList(serieRepository.findAll());
    }

    @Override
    public SerieDto buscarSerie(Long id) {
        if (serieRepository.findById(id).isPresent()) {
            return serieMapper.toDto(serieRepository.findById(id).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public SerieDto agregarSerie(SerieDto serieDto) {
        if (serieDto.getSerieId() == null) {
            return serieMapper.toDto(serieRepository.save(serieMapper.toEntity(serieDto)));
        }
        if (serieRepository.existsById(serieDto.getSerieId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public SerieDto modificarSerie(SerieDto serieDto) {
        if (serieRepository.existsById(serieDto.getSerieId())) {
            return serieMapper.toDto(serieRepository.save(serieMapper.toEntity(serieDto)));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public boolean eliminarSerie(Long id) {
        if (serieRepository.existsById(id)) {
            serieRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

}
