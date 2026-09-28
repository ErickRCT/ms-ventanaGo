package com.ventanago.serie.service.mapper;


import com.ventanago.serie.repository.entity.Serie;
import com.ventanago.serie.service.dto.SerieDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SerieMapper {

    List<SerieDto> toDtoList(List<Serie> serie);

    SerieDto toDto(Serie serie);

    Serie toEntity(SerieDto serieDto);
}
