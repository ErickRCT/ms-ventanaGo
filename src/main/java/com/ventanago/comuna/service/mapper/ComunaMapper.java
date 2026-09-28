package com.ventanago.comuna.service.mapper;

import com.ventanago.comuna.repository.entity.Comuna;
import com.ventanago.comuna.service.dto.ComunaDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ComunaMapper {

    List<ComunaDto> toDtoList(List<Comuna> comunas);

    ComunaDto toDto(Comuna comuna);

    Comuna toEntity(ComunaDto comunaDto);

}
