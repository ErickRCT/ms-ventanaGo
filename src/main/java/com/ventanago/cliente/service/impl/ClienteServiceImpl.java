package com.ventanago.cliente.service.impl;

import com.ventanago.cliente.repository.ClienteRepository;
import com.ventanago.cliente.service.ClienteService;
import com.ventanago.cliente.service.dto.ClienteDto;
import com.ventanago.cliente.service.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    private final ClienteMapper clienteMapper;

    @Override
    public List<ClienteDto> obtenerClientes() {
        return clienteMapper.toDtoList(clienteRepository.findAll());
    }

    @Override
    public ClienteDto obtenerClientePorId(Long id) {
        if(clienteRepository.findById(id).isPresent()) {
            return clienteMapper.toDto(clienteRepository.findById(id).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ClienteDto agregarCliente(ClienteDto clienteDto) {
        if(clienteDto.getClienteId() == null) {
            return clienteMapper.toDto(clienteRepository.save(clienteMapper.toEntity(clienteDto)));
        }
        if(clienteRepository.findById(clienteDto.getClienteId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ClienteDto modificarCliente(ClienteDto clienteDto){
        if(clienteRepository.existsById(clienteDto.getClienteId())) {
            return clienteMapper.toDto(clienteRepository.save(clienteMapper.toEntity(clienteDto)));
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Override
    public boolean eliminarCliente(Long id){
        if(clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        } else {
           return false;
        }
    }



}
