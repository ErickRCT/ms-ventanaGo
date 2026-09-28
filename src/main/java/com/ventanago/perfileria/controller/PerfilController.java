package com.ventanago.perfileria.controller;

import com.ventanago.perfileria.service.PerfilService;
import com.ventanago.perfileria.service.dto.PerfilDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    public ResponseEntity<List<PerfilDto>> getPerfiles() {
        return ResponseEntity.ok(perfilService.obtenerPerfiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilDto> getPerfil(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.buscarPerfil(id));
    }

    @PostMapping(value = "/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<PerfilDto> agregarPerfil(@RequestBody PerfilDto perfilDto) {
        return ResponseEntity.ok(perfilService.agregarPerfil(perfilDto));
    }

    @PutMapping(value = "/modificar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<PerfilDto> modificarPerfil(@RequestBody PerfilDto perfilDto) {
        return ResponseEntity.ok(perfilService.modificarPerfil(perfilDto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarPerfil(@PathVariable Long id) {
        return perfilService.eliminarPerfil(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("serie/{id}")
    public ResponseEntity<List<PerfilDto>> getPerfilesBySerie(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.obtenerPerfilesPorSerieId(id));
    }

    @GetMapping("tipo-perfil/{id}")
    public ResponseEntity<List<PerfilDto>> getPerfilesByTipoPerfiId(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.obtenerPerfilesPorTipoPerfilId(id));
    }

}
