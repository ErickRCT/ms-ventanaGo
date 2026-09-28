package com.ventanago.pauta.service.mapper;


import com.ventanago.pauta.repository.entity.TipoPauta;
import com.ventanago.pauta.service.dto.TipoPautaDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TipoPautaMapper {

    List<TipoPautaDto> toDtoList(List<TipoPauta> tipoPautas);

    TipoPautaDto toDto(TipoPauta tipoPauta);

    TipoPauta toEntity(TipoPautaDto tipoPautaDto);

}
