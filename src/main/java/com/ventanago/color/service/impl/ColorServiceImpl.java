package com.ventanago.color.service.impl;

import com.ventanago.color.repository.ColorRepository;
import com.ventanago.color.service.ColorService;
import com.ventanago.color.service.dto.ColorDto;
import com.ventanago.color.service.mapper.ColorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorServiceImpl implements ColorService {

    private final ColorRepository colorRepository;

    private final ColorMapper colorMapper;

    @Override
    public List<ColorDto> obtenerColores(){
        return colorMapper.toDtoList(colorRepository.findAll());
    }

    @Override
    public ColorDto obtenerColor(Long id){
        if(colorRepository.findById(id).isPresent()) {
            return colorMapper.toDto(colorRepository.findById(id).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ColorDto agregarColor(ColorDto colorDto){
        if(colorDto.getColorId() == null) {
            return colorMapper.toDto(colorRepository.save(colorMapper.toEntity(colorDto)));
        }
        if(colorRepository.existsById(colorDto.getColorId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ColorDto modificarColor(ColorDto colorDto){
        if(colorRepository.existsById(colorDto.getColorId())) {
            return colorMapper.toDto(colorRepository.save(colorMapper.toEntity(colorDto)));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public boolean eliminarColor(Long id){
        if(colorRepository.existsById(id)) {
            colorRepository.deleteById(id);
            return true;
        } else {
            return false;
        }}

}
