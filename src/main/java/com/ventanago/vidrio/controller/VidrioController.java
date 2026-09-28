package com.ventanago.vidrio.controller;

import com.ventanago.vidrio.service.VidrioService;
import com.ventanago.vidrio.service.dto.VidrioDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vidrio")
@RequiredArgsConstructor
public class VidrioController {

    private final VidrioService vidrioService;


    @GetMapping
    public ResponseEntity<List<VidrioDto>> obtenerVidrios() {
        return ResponseEntity.ok(vidrioService.obtenerVidrios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VidrioDto> obtenerVidrio(@PathVariable Long id) {
        return ResponseEntity.ok(vidrioService.obtenerVidrio(id));
    }

    @PostMapping(value = "/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<VidrioDto> agregarVidrio(@RequestBody VidrioDto vidrioDto){
        return ResponseEntity.ok(vidrioService.agregarVidrio(vidrioDto));
    }

    @PutMapping(value = "/modificar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<VidrioDto> modificarVidrio(@RequestBody VidrioDto vidrioDto){
        return ResponseEntity.ok(vidrioService.modificarVidrio(vidrioDto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarVidrio(@PathVariable Long id){
        return vidrioService.eliminarVidrio(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
