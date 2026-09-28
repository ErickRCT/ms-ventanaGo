package com.ventanago.pauta.service.mapper;

import com.ventanago.pauta.repository.entity.PautaPerfil;
import com.ventanago.pauta.service.dto.PautaPerfilDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PautaPerfilMapper {

    PautaPerfilDto toDto(PautaPerfil pautaPerfil);

    @Mapping(target = "pauta.pautaId", source = "pautaId")
    PautaPerfil toEntity(PautaPerfilDto pautaPerfilDto);

}
