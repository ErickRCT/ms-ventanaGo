package com.ventanago.quincalleria.controller;


import com.ventanago.quincalleria.service.QuincalleriaService;
import com.ventanago.quincalleria.service.dto.QuincalleriaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quincalleria")
@RequiredArgsConstructor
public class QuincalleriaController {

    private final QuincalleriaService quincalleriaService;


    @GetMapping
    public ResponseEntity<List<QuincalleriaDto>> obtenerQuincallerias() {
        return ResponseEntity.ok(quincalleriaService.obtenerQuincallerias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuincalleriaDto> obtenerQuincalleria(@PathVariable Long id) {
        return ResponseEntity.ok(quincalleriaService.obtenerQuincalleria(id));
    }

    @PostMapping(value ="/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<QuincalleriaDto> agregarQuincalleria(@RequestBody QuincalleriaDto quincalleriaDto){
        return ResponseEntity.ok(quincalleriaService.agregarQuincalleria(quincalleriaDto));
    }

    @PutMapping(value = "/modificar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<QuincalleriaDto> modificarQuincalleria(@RequestBody QuincalleriaDto quincalleriaDto) {
        return ResponseEntity.ok(quincalleriaService.modificarQuincalleria(quincalleriaDto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarQuincalleria(@PathVariable Long id) {
        return quincalleriaService.eliminarQuincalleria(id) ?
                ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping(value="/serie/{id}")
    public ResponseEntity<List<QuincalleriaDto>> obtenerQuincalleriasPorSerieId(@PathVariable Long id) {
        return ResponseEntity.ok(quincalleriaService.obtenerQuincalleriasPorSerieId(id));
    }
}
