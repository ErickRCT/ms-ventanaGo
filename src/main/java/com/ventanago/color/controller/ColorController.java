package com.ventanago.color.controller;


import com.ventanago.color.service.ColorService;
import com.ventanago.color.service.dto.ColorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/color")
@RequiredArgsConstructor
public class ColorController {

    private final ColorService colorService;

    @GetMapping
    public ResponseEntity<List<ColorDto>> obtenerColores() {
        return ResponseEntity.ok(colorService.obtenerColores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColorDto> obtenerColor(@PathVariable Long id) {
        return ResponseEntity.ok(colorService.obtenerColor(id));
    }

    @PostMapping(value = "/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ColorDto> agregarColor(@RequestBody ColorDto colorDto) {
        return ResponseEntity.ok(colorService.agregarColor(colorDto));
    }

    @PutMapping(value = "/modificar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ColorDto> modificarColor(@RequestBody ColorDto colorDto) {
        return ResponseEntity.ok(colorService.modificarColor(colorDto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarColor(@PathVariable Long id) {
        return colorService.eliminarColor(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
