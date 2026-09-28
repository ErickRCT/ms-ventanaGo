package com.ventanago.perfileria.controller;

import com.ventanago.perfileria.service.TipoPerfilService;
import com.ventanago.perfileria.service.dto.TipoPerfilDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipo-perfil")
@RequiredArgsConstructor
public class TipoPerfilController {

    private final TipoPerfilService tipoPerfilService;

    @GetMapping
    public ResponseEntity<List<TipoPerfilDto>> getTipoPerfiles() {
        return ResponseEntity.ok(tipoPerfilService.getTipoPerfiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoPerfilDto> getTipoPerfilById(@PathVariable Long id) {
        return ResponseEntity.ok(tipoPerfilService.buscarTipoPerfil(id));
    }

    @PostMapping(value = "/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TipoPerfilDto> agregarTipoPerfil(@RequestBody TipoPerfilDto tipoPerfilDto) {
        return ResponseEntity.ok(tipoPerfilService.agregarTipoPerfil(tipoPerfilDto));
    }

    @PutMapping(value = "/modificar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TipoPerfilDto> modificarTipoPerfil(@RequestBody TipoPerfilDto tipoPerfilDto) {
        return ResponseEntity.ok(tipoPerfilService.modificarTipoPerfil(tipoPerfilDto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarTipoPerfil(@PathVariable Long id) {
        return tipoPerfilService.eliminarTipoPerfil(id) ?
                ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
