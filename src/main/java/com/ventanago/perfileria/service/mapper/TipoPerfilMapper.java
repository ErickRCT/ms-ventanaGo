package com.ventanago.perfileria.service.mapper;

import com.ventanago.perfileria.repository.entity.TipoPerfil;
import com.ventanago.perfileria.service.dto.TipoPerfilDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TipoPerfilMapper {

    List<TipoPerfilDto> toDtoList(List<TipoPerfil> tipoPerfil);

    TipoPerfilDto toDto(TipoPerfil tipoPerfil);

    TipoPerfil toEntity(TipoPerfilDto tipoPerfilDto);
}
