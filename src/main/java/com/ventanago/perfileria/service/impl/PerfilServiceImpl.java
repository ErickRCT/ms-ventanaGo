package com.ventanago.perfileria.service.impl;

import com.ventanago.perfileria.repository.PerfilRepository;
import com.ventanago.perfileria.repository.entity.Perfil;
import com.ventanago.perfileria.service.PerfilService;
import com.ventanago.perfileria.service.TipoPerfilService;
import com.ventanago.perfileria.service.dto.PerfilDto;
import com.ventanago.perfileria.service.mapper.PerfilMapper;
import com.ventanago.serie.service.SerieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository perfilRepository;

    private final SerieService serieService;

    private final TipoPerfilService tipoPerfilService;

    private final PerfilMapper perfilMapper;

    @Override
    public List<PerfilDto> obtenerPerfiles() {
        return perfilMapper.toDtoList(perfilRepository.findAll());
    }

    @Override
    public PerfilDto buscarPerfil(Long id){
        if(perfilRepository.findById(id).isPresent()) {
            return perfilMapper.toDto(perfilRepository.findById(id).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public PerfilDto agregarPerfil(PerfilDto perfilDto) {
        if(perfilDto.getPerfilId() == null){
            perfilDto.setIsBastidor(false);
            perfilDto.setTipoPerfil(tipoPerfilService.buscarTipoPerfil(perfilDto.getTipoPerfil().getTipoPerfilId()));
            perfilDto.setSerie(serieService.buscarSerie(perfilDto.getSerie().getSerieId()));
            Perfil perfil =  perfilMapper.toEntity(perfilDto);
            return perfilMapper.toDto(perfilRepository.save(perfil));
        }
        if(perfilRepository.findById(perfilDto.getPerfilId()).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public PerfilDto modificarPerfil(PerfilDto perfilDto){
        if(perfilRepository.findById(perfilDto.getPerfilId()).isPresent()){
            return perfilMapper.toDto(perfilRepository.save(perfilMapper.toEntity(perfilDto)));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public boolean eliminarPerfil(Long id){
        if(perfilRepository.findById(id).isPresent()){
            perfilRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<PerfilDto> obtenerPerfilesPorSerieId(Long serieId){
       return perfilMapper.toDtoList(perfilRepository.findBySerie_SerieId(serieId));
    }

    @Override
    public List<PerfilDto> obtenerPerfilesPorTipoPerfilId(Long tipoPerfilId){
        return perfilMapper.toDtoList(perfilRepository.findByTipoPerfil_TipoPerfilId(tipoPerfilId));
    }


}
