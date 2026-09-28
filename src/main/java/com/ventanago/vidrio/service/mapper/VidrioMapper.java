package com.ventanago.vidrio.service.mapper;

import com.ventanago.vidrio.repository.entity.Vidrio;
import com.ventanago.vidrio.service.dto.VidrioDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VidrioMapper {

    List<VidrioDto> toDtoList(List<Vidrio> vidrio);

    VidrioDto toDto(Vidrio vidrio);

    Vidrio toEntity(VidrioDto vidrioDto);

}
