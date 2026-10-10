package com.ventanago.proveedor.controller;

import com.ventanago.auth.SesionActual;
import com.ventanago.proveedor.service.ProveedorService;
import com.ventanago.proveedor.service.dto.ProveedorDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Los permisos por rol de cada ruta están en SeguridadConfig. */
@RestController
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    /** Público: proveedores que atienden una comuna y un servicio. */
    @GetMapping("/proveedores")
    public List<ProveedorResumenDto> listar(@RequestParam(required = false) Long comunaId, @RequestParam(required = false) String servicio) {
        return proveedorService.listar(comunaId, servicio);
    }

    /** Público: ficha con las últimas valoraciones. */
    @GetMapping("/proveedores/{id}")
    public ProveedorDetalleDto detalle(@PathVariable Long id) {
        return proveedorService.detalle(id);
    }

    /** Administrador: marca al proveedor como verificado. */
    @PutMapping("/proveedores/{id}/verificado")
    public ResponseEntity<Void> verificar(@PathVariable Long id, @RequestBody VerificadoRequest request) {
        proveedorService.verificar(id, request.verificado());
        return ResponseEntity.noContent().build();
    }

    /** Proveedor: su propio perfil. */
    @GetMapping("/proveedor/perfil")
    public PerfilDto perfil(@AuthenticationPrincipal Jwt jwt) {
        return proveedorService.perfil(SesionActual.de(jwt).cuentaId());
    }

    @PutMapping("/proveedor/perfil")
    public PerfilDto guardar(@AuthenticationPrincipal Jwt jwt, @RequestBody PerfilRequest datos) {
        return proveedorService.guardarPerfil(SesionActual.de(jwt).cuentaId(), datos);
    }
}
