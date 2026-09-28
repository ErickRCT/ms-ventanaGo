package com.ventanago.pauta.service.impl;

import com.ventanago.pauta.repository.PautaRepository;
import com.ventanago.pauta.repository.entity.Pauta;
import com.ventanago.pauta.repository.entity.PautaPerfil;
import com.ventanago.pauta.repository.entity.PautaQuincalleria;
import com.ventanago.pauta.repository.entity.PautaVidrio;
import com.ventanago.pauta.service.PautaService;
import com.ventanago.pauta.service.dto.PautaDto;
import com.ventanago.pauta.service.mapper.PautaMapper;
import com.ventanago.pauta.service.mapper.PautaPerfilMapper;
import com.ventanago.pauta.service.mapper.PautaQuincalleriaMapper;
import com.ventanago.pauta.service.mapper.PautaVidrioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PautaServiceImpl implements PautaService {

    private final PautaRepository pautaRepository;

    private final PautaMapper pautaMapper;

    private final PautaPerfilMapper pautaPerfilMapper;

    private final PautaVidrioMapper pautaVidrioMapper;

    private final PautaQuincalleriaMapper pautaQuincalleriaMapper;

    @Transactional
    @Override
    public List<PautaDto> obtenerPautas(){
        return pautaMapper.toDtoList(pautaRepository.findAll());
    }

    @Transactional
    @Override
    public PautaDto obtenerPautasPorId(Long id){
        if (pautaRepository.findById(id).isPresent()){
            Pauta pauta = pautaRepository.findById(id).get();
            return pautaMapper.toDto(pauta);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public List<PautaDto> obtenerPautasPorSerieId(Long idSerie){
        return pautaMapper.toDtoList(pautaRepository.findBySerie_SerieId(idSerie));
    }

    @Transactional
    @Override
    public PautaDto agregarPauta(PautaDto pautaDto){
        if (pautaDto.getPautaId() == null){
            Pauta pauta = pautaRepository.save(pautaMapper.toBasicEmpty(pautaDto));

            if (pautaDto.getVidrios() != null) {
                pauta.setVidrios(pautaDto.getVidrios().stream().map(pautaVidrioDto -> {
                    PautaVidrio pautaVidrio = pautaVidrioMapper.toEntity(pautaVidrioDto);
                    pautaVidrio.setPauta(pauta);
                    return pautaVidrio;
                }).collect(Collectors.toSet()));
            }

            if (pautaDto.getQuincallerias() != null) {
                pauta.setQuincallerias(pautaDto.getQuincallerias().stream().map(pautaQuincalleriaDto -> {
                    PautaQuincalleria pautaQuincalleria = pautaQuincalleriaMapper.toEntity(pautaQuincalleriaDto);
                    pautaQuincalleria.setPauta(pauta);
                    return pautaQuincalleria;
                }).collect(Collectors.toSet()));
            }

            if (pautaDto.getPerfiles() != null) {
                pauta.setPerfiles(pautaDto.getPerfiles().stream().map(perfilDto -> {
                    PautaPerfil pautaPerfil = pautaPerfilMapper.toEntity(perfilDto);
                    pautaPerfil.setPauta(pauta);
                    return pautaPerfil;
                }).collect(Collectors.toSet()));
            }
            return pautaMapper.toDto(pautaRepository.save(pauta));
        }

        if (pautaRepository.findById(pautaDto.getPautaId()).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public PautaDto modificarPauta(PautaDto pautaDto){
        if (pautaRepository.existsById(pautaDto.getPautaId())){
            return pautaMapper.toDto(pautaRepository.save(pautaMapper.toEntity(pautaDto)));
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public boolean eliminarPauta(Long id) {
        if(pautaRepository.existsById(id)){
            pautaRepository.deleteById(id);
            return true;
        }
        return false;

    }

}
