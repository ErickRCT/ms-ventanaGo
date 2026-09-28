package com.ventanago.pauta.service.mapper;

import com.ventanago.pauta.repository.entity.PautaVidrio;
import com.ventanago.pauta.service.dto.PautaVidrioDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PautaVidrioMapper {

    PautaVidrio toEntity(PautaVidrioDto pautaVidrioDto);

    PautaVidrioDto toDto(PautaVidrio pauta);
}
