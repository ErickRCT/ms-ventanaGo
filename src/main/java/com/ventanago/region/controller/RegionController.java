package com.ventanago.region.controller;

import com.ventanago.region.service.RegionService;
import com.ventanago.region.service.dto.RegionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/region")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @GetMapping
    public ResponseEntity<List<RegionDto>> obtenerRegiones(){
        return ResponseEntity.ok(regionService.obtenerRegiones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionDto> obtenerRegion(@PathVariable Long id){
        return ResponseEntity.ok(regionService.buscarRegion(id));
    }
}
