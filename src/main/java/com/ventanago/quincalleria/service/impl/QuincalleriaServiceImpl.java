package com.ventanago.quincalleria.service.impl;

import com.ventanago.quincalleria.repository.QuincalleriaRepository;
import com.ventanago.quincalleria.service.QuincalleriaService;
import com.ventanago.quincalleria.service.dto.QuincalleriaDto;
import com.ventanago.quincalleria.service.mapper.QuincalleriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuincalleriaServiceImpl implements QuincalleriaService {

    private final QuincalleriaRepository quincalleriaRepository;

    private final QuincalleriaMapper quincalleriaMapper;

    @Override
    public List<QuincalleriaDto> obtenerQuincallerias() {
        return  quincalleriaMapper.toDtoList(quincalleriaRepository.findAll());
    }

    @Override
    public QuincalleriaDto obtenerQuincalleria(Long id) {
        if(quincalleriaRepository.findById(id).isPresent()){
            return quincalleriaMapper.toDto(quincalleriaRepository.findById(id).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public QuincalleriaDto agregarQuincalleria(QuincalleriaDto quincalleriaDto) {
        if (quincalleriaDto.getQuincalleriaId() == null) {
            return quincalleriaMapper.toDto(quincalleriaRepository
                    .save(quincalleriaMapper.toEntity(quincalleriaDto)));
        }
        if (quincalleriaRepository.existsById(quincalleriaDto.getQuincalleriaId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public QuincalleriaDto modificarQuincalleria(QuincalleriaDto quincalleriaDto) {
        if (quincalleriaRepository.existsById(quincalleriaDto.getQuincalleriaId())) {
            return quincalleriaMapper.toDto(quincalleriaRepository
                    .save(quincalleriaMapper.toEntity(quincalleriaDto)));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public boolean eliminarQuincalleria(Long id) {
        if (quincalleriaRepository.existsById(id)) {
            quincalleriaRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<QuincalleriaDto> obtenerQuincalleriasPorSerieId(Long serieId) {
        return quincalleriaMapper.toDtoList(quincalleriaRepository.findBySerie_SerieIdOrSerieIsNull(serieId));
    }


}
