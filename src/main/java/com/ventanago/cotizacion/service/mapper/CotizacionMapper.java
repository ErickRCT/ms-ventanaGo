package com.ventanago.cotizacion.service.mapper;

import com.ventanago.cotizacion.repository.entity.Cotizacion;
import com.ventanago.cotizacion.service.dto.CotizacionDto;
import com.ventanago.utils.RutUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { RutUtils.class })
public interface CotizacionMapper {

    List<CotizacionDto> toDtoList(List<Cotizacion> cotizaciones);

    @Mapping(source = "cliente.rut", target = "cliente.rut", qualifiedByName = "formatearRut")
    CotizacionDto toDto(Cotizacion cotizacion);

    Cotizacion toEntity(CotizacionDto cotizacionDto);

}
