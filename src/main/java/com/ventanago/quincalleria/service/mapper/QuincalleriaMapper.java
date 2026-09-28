package com.ventanago.quincalleria.service.mapper;


import com.ventanago.quincalleria.repository.entity.Quincalleria;
import com.ventanago.quincalleria.service.dto.QuincalleriaDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuincalleriaMapper {

    List<QuincalleriaDto> toDtoList(List<Quincalleria> quincalleriaList);

    QuincalleriaDto toDto(Quincalleria quincalleria);

    Quincalleria toEntity(QuincalleriaDto quincalleriaDto);


}
