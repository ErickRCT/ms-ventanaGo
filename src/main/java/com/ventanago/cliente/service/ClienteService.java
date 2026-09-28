package com.ventanago.cliente.service;

import com.ventanago.cliente.service.dto.ClienteDto;

import java.util.List;

public interface ClienteService {
    List<ClienteDto> obtenerClientes();

    ClienteDto obtenerClientePorId(Long id);

    ClienteDto agregarCliente(ClienteDto clienteDto);

    ClienteDto modificarCliente(ClienteDto clienteDto);

    boolean eliminarCliente(Long id);
}
