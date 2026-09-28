package com.ventanago.cliente.service.mapper;

import com.ventanago.cliente.repository.entity.Cliente;
import com.ventanago.cliente.service.dto.ClienteDto;
import com.ventanago.utils.RutUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { RutUtils.class })
public interface ClienteMapper {


    List<ClienteDto> toDtoList(List<Cliente> clientes);

    @Mapping(source = "rut", target = "rut", qualifiedByName = "formatearRut")
    ClienteDto toDto(Cliente cliente);

    Cliente toEntity(ClienteDto cliente);

}
