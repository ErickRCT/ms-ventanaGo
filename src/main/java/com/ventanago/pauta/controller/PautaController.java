package com.ventanago.pauta.controller;

import com.ventanago.pauta.service.PautaService;
import com.ventanago.pauta.service.dto.PautaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pauta")
@RequiredArgsConstructor
public class PautaController {

    private final PautaService pautaService;

    @GetMapping
    public ResponseEntity<List<PautaDto>> obtenerPautas(){
        return ResponseEntity.ok(pautaService.obtenerPautas());
    }


    @GetMapping("/{id}")
    public ResponseEntity<PautaDto> obtenerPautasPorId(@PathVariable Long id){
        return ResponseEntity.ok(pautaService.obtenerPautasPorId(id));
    }

    @GetMapping("/serie/{id}")
    public ResponseEntity<List<PautaDto>> obtenerPautasPorSerieId(@PathVariable Long id){
        return ResponseEntity.ok(pautaService.obtenerPautasPorSerieId(id));
    }

    @PostMapping(value ="/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<PautaDto> agregarPauta(@RequestBody PautaDto pautaDto){
        return ResponseEntity.ok(pautaService.agregarPauta(pautaDto));
    }

    @PutMapping(value = "/modificar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<PautaDto> modificarPauta(@RequestBody PautaDto pautaDto){
        return ResponseEntity.ok(pautaService.modificarPauta(pautaDto));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarPauta(@PathVariable Long id){
        return pautaService.eliminarPauta(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
