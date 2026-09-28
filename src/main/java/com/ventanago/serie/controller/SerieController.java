package com.ventanago.serie.controller;


import com.ventanago.serie.service.SerieService;
import com.ventanago.serie.service.dto.SerieDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/serie")
@RequiredArgsConstructor
public class SerieController {

    private final SerieService serieService;

    @GetMapping
    public ResponseEntity<List<SerieDto>> obtenerSeries() {
        return ResponseEntity.ok(serieService.obtenerSeries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SerieDto> obtenerSerie(@PathVariable Long id) {
        return ResponseEntity.ok(serieService.buscarSerie(id));
    }

    @PostMapping(value = "/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SerieDto> agregarSerie(@RequestBody SerieDto serieDto) {
        return ResponseEntity.ok(serieService.agregarSerie(serieDto));
    }

    @PutMapping(value = "/modificar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SerieDto> modificarSerie(@RequestBody SerieDto serieDto) {
        return ResponseEntity.ok(serieService.modificarSerie(serieDto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarSerie(@PathVariable Long id) {
        return serieService.eliminarSerie(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


}
