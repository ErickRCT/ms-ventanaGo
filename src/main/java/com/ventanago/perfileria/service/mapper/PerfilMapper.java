package com.ventanago.perfileria.service.mapper;

import com.ventanago.perfileria.repository.entity.Perfil;
import com.ventanago.perfileria.service.dto.PerfilDto;
import org.mapstruct.Mapper;
import java.util.List;


@Mapper(componentModel = "spring")
public interface PerfilMapper {

    List<PerfilDto> toDtoList(List<Perfil> perfil);

    PerfilDto toDto(Perfil perfil);

    Perfil toEntity(PerfilDto perfilDto);

}
