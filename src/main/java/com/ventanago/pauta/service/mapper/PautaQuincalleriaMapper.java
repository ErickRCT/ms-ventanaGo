package com.ventanago.pauta.service.mapper;


import com.ventanago.pauta.repository.entity.PautaQuincalleria;
import com.ventanago.pauta.service.dto.PautaQuincalleriaDto;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface PautaQuincalleriaMapper {

    PautaQuincalleria toEntity(PautaQuincalleriaDto quincalleriaDto);

    PautaQuincalleriaDto toDto(PautaQuincalleria quincalleria);
}
