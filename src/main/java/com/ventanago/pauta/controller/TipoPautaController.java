package com.ventanago.pauta.controller;

import com.ventanago.pauta.service.TipoPautaService;
import com.ventanago.pauta.service.dto.TipoPautaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipo-pauta")
@RequiredArgsConstructor
public class TipoPautaController {

    private final TipoPautaService tipoPautaService;

    @GetMapping
    public ResponseEntity<List<TipoPautaDto>> obtenerTipoPautas(){
        return ResponseEntity.ok(tipoPautaService.obtenerTipoPautas());
    }

    @PostMapping(value ="/agregar", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TipoPautaDto> agregarTipoPauta(@RequestBody TipoPautaDto tipoPautaDto){
        return ResponseEntity.ok(tipoPautaService.agregarTipoPauta(tipoPautaDto));
    }
}
