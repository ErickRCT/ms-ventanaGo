package com.ventanago.pauta.service.impl;

import com.ventanago.pauta.repository.TipoPautaRepository;
import com.ventanago.pauta.service.TipoPautaService;
import com.ventanago.pauta.service.dto.TipoPautaDto;
import com.ventanago.pauta.service.mapper.TipoPautaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoPautaServiceImpl implements TipoPautaService {

    private final TipoPautaRepository tipoPautaRepository;

    private final TipoPautaMapper tipoPautaMapper;

    @Transactional
    @Override
    public List<TipoPautaDto> obtenerTipoPautas(){
        return tipoPautaMapper.toDtoList(tipoPautaRepository.findAll());
    }

    @Transactional
    @Override
    public TipoPautaDto agregarTipoPauta(TipoPautaDto tipoPautaDto){
        if(tipoPautaDto.getTipoPautaId() == null){
            return tipoPautaMapper.toDto(tipoPautaRepository
                    .save(tipoPautaMapper.toEntity(tipoPautaDto)));
        }
        if(tipoPautaRepository.existsById(tipoPautaDto.getTipoPautaId())){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
