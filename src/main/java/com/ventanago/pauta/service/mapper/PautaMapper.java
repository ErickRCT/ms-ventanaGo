package com.ventanago.pauta.service.mapper;

import com.ventanago.pauta.repository.entity.Pauta;
import com.ventanago.pauta.repository.entity.PautaPerfil;
import com.ventanago.pauta.repository.entity.PautaQuincalleria;
import com.ventanago.pauta.service.dto.PautaDto;
import com.ventanago.pauta.service.dto.PautaPerfilDto;
import com.ventanago.pauta.service.dto.PautaQuincalleriaDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PautaMapper {

    List<PautaDto> toDtoList(List<Pauta> pautas);

    PautaDto toDto(Pauta pauta);

    PautaQuincalleriaDto toDto(PautaQuincalleria pauta);

    Pauta toEntity(PautaDto pautaDto);

    @Mapping(target = "perfiles", ignore = true)
    @Mapping(target = "quincallerias", ignore = true)
    @Mapping(target = "vidrios ", ignore = true)
    Pauta toBasicEmpty(PautaDto pautaDto);

    @Mapping(target = "pauta.pautaId", source = "pautaId")
    PautaPerfil toEntity(PautaPerfilDto pautaPerfilDto);


}
