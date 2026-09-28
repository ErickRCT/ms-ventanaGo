package com.ventanago.cotizacion.service.impl;

import com.ventanago.cotizacion.repository.CotizacionRepository;
import com.ventanago.cotizacion.repository.entity.Cotizacion;
import com.ventanago.cotizacion.service.CotizacionService;
import com.ventanago.cotizacion.service.dto.CotizacionDto;
import com.ventanago.cotizacion.service.mapper.CotizacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl implements CotizacionService {

    private final CotizacionRepository cotizacionRepository;

    private final CotizacionMapper cotizacionMapper;

    @Transactional
    @Override
    public List<CotizacionDto> obtenerCotizaciones(){
        List<Cotizacion> cotizaciones = cotizacionRepository.findAll();
        return cotizacionMapper.toDtoList(cotizaciones);
    }

    @Transactional
    @Override
    public CotizacionDto obtenerCotizacion(Long id){
        if (cotizacionRepository.findById(id).isPresent()){
            return cotizacionMapper.toDto(cotizacionRepository.findById(id).get());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public CotizacionDto agregarCotizacion(CotizacionDto cotizacionDto){
        if (cotizacionDto.getCotizacionId() == null){
            return cotizacionMapper.toDto(cotizacionRepository.save(cotizacionMapper.toEntity(cotizacionDto)));
        }
        if (cotizacionRepository.findById(cotizacionDto.getCotizacionId()).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public CotizacionDto modificarCotizacion(CotizacionDto cotizacionDto){
        if(cotizacionRepository.existsById(cotizacionDto.getCotizacionId())){
            return cotizacionMapper.toDto(cotizacionRepository.save(cotizacionMapper.toEntity(cotizacionDto)));
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    //TODO: Hacer funcion para calcular el M2Total y cantidad de productos..


}
