package com.ventanago.cotizacion.controller;

import com.ventanago.cotizacion.service.CotizacionService;
import com.ventanago.cotizacion.service.dto.CotizacionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cotizacion")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @GetMapping
    public ResponseEntity<List<CotizacionDto>> obtenerCotizaciones() {
        return ResponseEntity.ok(cotizacionService.obtenerCotizaciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CotizacionDto> obtenerCotizacion(@PathVariable Long id) {
        return ResponseEntity.ok(cotizacionService.obtenerCotizacion(id));
    }

    @PostMapping(value ="/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CotizacionDto> agregarCotizacion(@RequestBody CotizacionDto cotizacion) {
        return ResponseEntity.ok(cotizacionService.agregarCotizacion(cotizacion));
    }

    @PutMapping(value = "/modificar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CotizacionDto> modificarCotizacion(@RequestBody CotizacionDto cotizacion) {
        return ResponseEntity.ok(cotizacionService.modificarCotizacion(cotizacion));
    }

}
