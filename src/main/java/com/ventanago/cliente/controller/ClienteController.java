package com.ventanago.cliente.controller;

import com.ventanago.cliente.service.ClienteService;
import com.ventanago.cliente.service.dto.ClienteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteDto>> listarClientes() {
        return ResponseEntity.ok(clienteService.obtenerClientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDto> obtenerCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerClientePorId(id));
    }

    @PostMapping(value = "/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ClienteDto> agregarCliente(@RequestBody ClienteDto clienteDto) {
        return ResponseEntity.ok(clienteService.agregarCliente(clienteDto));
    }

    @PutMapping(value = "/modificar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ClienteDto> modificarCliente(@RequestBody ClienteDto clienteDto) {
        return ResponseEntity.ok(clienteService.modificarCliente(clienteDto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        return clienteService.eliminarCliente(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
