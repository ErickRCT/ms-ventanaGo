package com.ventanago.perfileria.service.impl;

import com.ventanago.perfileria.repository.TipoPerfilRepository;
import com.ventanago.perfileria.service.TipoPerfilService;
import com.ventanago.perfileria.service.dto.TipoPerfilDto;
import com.ventanago.perfileria.service.mapper.TipoPerfilMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoPerfilServiceImpl implements TipoPerfilService {

    private final TipoPerfilRepository tipoPerfilRepository;

    private final TipoPerfilMapper tipoPerfilMapper;

    @Override
    public List<TipoPerfilDto> getTipoPerfiles() {
        return tipoPerfilMapper.toDtoList(tipoPerfilRepository.findAll());
    }

    @Override
    public TipoPerfilDto buscarTipoPerfil(Long id){
        if(tipoPerfilRepository.findById(id).isPresent()){
            return tipoPerfilMapper.toDto(tipoPerfilRepository.findById(id).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public TipoPerfilDto agregarTipoPerfil(TipoPerfilDto tipoPerfilDto){
        if (tipoPerfilDto.getTipoPerfilId() == null){
            return tipoPerfilMapper.toDto(tipoPerfilRepository.save(tipoPerfilMapper.toEntity(tipoPerfilDto)));
        }
        if(tipoPerfilRepository.existsById(tipoPerfilDto.getTipoPerfilId())){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public TipoPerfilDto modificarTipoPerfil(TipoPerfilDto tipoPerfilDto){
        if (tipoPerfilRepository.existsById(tipoPerfilDto.getTipoPerfilId())){
            return tipoPerfilMapper.toDto(tipoPerfilRepository.save(tipoPerfilMapper.toEntity(tipoPerfilDto)));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public boolean eliminarTipoPerfil(Long id){
        if(tipoPerfilRepository.findById(id).isPresent()){
            tipoPerfilRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

}
