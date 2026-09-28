package com.ventanago.ventana.service.mapper;

import com.ventanago.ventana.repository.entity.Ventana;
import com.ventanago.ventana.service.dto.VentanaDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VentanaMapper {

    List<VentanaDto> toDtoList(List<Ventana> ventanas);

    @Mapping(source = "cotizacion.cotizacionId", target = "cotizacionId")
    VentanaDto toDto(Ventana ventana);

    @Mapping(source = "cotizacionId", target = "cotizacion.cotizacionId")
    Ventana toEntity(VentanaDto ventanaDto);


}
