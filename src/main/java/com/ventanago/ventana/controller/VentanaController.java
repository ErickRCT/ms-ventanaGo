package com.ventanago.ventana.controller;

import com.ventanago.ventana.service.VentanaService;
import com.ventanago.ventana.service.dto.VentanaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventana")
@RequiredArgsConstructor
public class VentanaController {

    private final VentanaService ventanaService;

    @GetMapping
    public ResponseEntity<List<VentanaDto>> obtenerVentanas(){
        return ResponseEntity.ok(ventanaService.obtenerVentanas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentanaDto> obtenerVentana(@PathVariable Long id){
        return ResponseEntity.ok(ventanaService.obtenerVentana(id));
    }

    @PostMapping(value ="/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<VentanaDto> cotizarYGuardarVentana(@RequestBody VentanaDto ventanaDto) {
        return ResponseEntity.ok(ventanaService.cotizarYGuardarVentana(ventanaDto));
    }

    @PostMapping(value ="/cotizar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<VentanaDto> cotizarVentana(@RequestBody VentanaDto ventanaDto) {
        return ResponseEntity.ok(ventanaService.cotizarVentana(ventanaDto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarVentana(@PathVariable Long id){
        return ventanaService.eliminarVentana(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }



}
