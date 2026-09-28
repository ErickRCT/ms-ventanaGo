package com.ventanago.comuna.controller;

import com.ventanago.comuna.service.ComunaService;
import com.ventanago.comuna.service.dto.ComunaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comuna")
@RequiredArgsConstructor
public class ComunaController {

    private final ComunaService comunaService;

    @GetMapping
    public ResponseEntity<List<ComunaDto>> listar() {
        return ResponseEntity.ok(comunaService.comunas());
    }

    @GetMapping("/region/{id}")
    public ResponseEntity<List<ComunaDto>> listarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comunaService.comunasPorRegion(id));
    }

}
