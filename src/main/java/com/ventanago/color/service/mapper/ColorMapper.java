package com.ventanago.color.service.mapper;

import com.ventanago.color.repository.entity.Color;
import com.ventanago.color.service.dto.ColorDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ColorMapper {

    List<ColorDto> toDtoList(List<Color> colorList);

    ColorDto toDto(Color color);

    Color toEntity(ColorDto colorDto);
}
