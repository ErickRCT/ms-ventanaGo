package com.ventanago.solicitud.controller;

import com.ventanago.auth.SesionActual;
import com.ventanago.solicitud.service.SolicitudService;
import com.ventanago.solicitud.service.dto.SolicitudDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Los permisos por rol de cada ruta están en SeguridadConfig. */
@RestController
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    // ---------- Carrito (cliente) ----------

    @GetMapping("/carrito")
    public List<ItemVentanaDto> carrito(@AuthenticationPrincipal Jwt jwt) {
        return solicitudService.carrito(SesionActual.de(jwt).cuentaId());
    }

    @PostMapping("/carrito")
    public List<ItemVentanaDto> agregar(@AuthenticationPrincipal Jwt jwt, @RequestBody ItemVentanaDto item) {
        return solicitudService.agregarAlCarrito(SesionActual.de(jwt).cuentaId(), item);
    }

    @PutMapping("/carrito/{id}/cantidad")
    public List<ItemVentanaDto> cambiarCantidad(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody CantidadRequest request) {
        return solicitudService.cambiarCantidad(SesionActual.de(jwt).cuentaId(), id, request.cantidad());
    }

    @DeleteMapping("/carrito/{id}")
    public List<ItemVentanaDto> quitar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return solicitudService.quitarDelCarrito(SesionActual.de(jwt).cuentaId(), id);
    }

    // ---------- Solicitudes ----------

    /** Cliente: las suyas. Proveedor y administrador: todas. */
    @GetMapping("/solicitudes")
    public List<SolicitudDto> solicitudes(@AuthenticationPrincipal Jwt jwt) {
        SesionActual sesion = SesionActual.de(jwt);
        return solicitudService.solicitudes(sesion.cuentaId(), sesion.rol());
    }

    /** Cliente: envía su carrito como solicitud. */
    @PostMapping("/solicitudes")
    public SolicitudDto enviar(@AuthenticationPrincipal Jwt jwt, @RequestBody NuevaSolicitudRequest datos) {
        return solicitudService.enviarSolicitud(SesionActual.de(jwt).cuentaId(), datos);
    }

    /** Proveedor: acepta, modifica o rechaza. */
    @PostMapping("/solicitudes/{numero}/respuesta")
    public SolicitudDto responder(@AuthenticationPrincipal Jwt jwt, @PathVariable Long numero, @RequestBody RespuestaRequest respuesta) {
        return solicitudService.responder(numero, SesionActual.de(jwt).cuentaId(), respuesta);
    }

    // ---------- Avisos ----------

    @GetMapping("/avisos")
    public List<AvisoDto> avisos(@AuthenticationPrincipal Jwt jwt) {
        SesionActual sesion = SesionActual.de(jwt);
        return solicitudService.avisos(SolicitudService.destinatarioDe(sesion.cuentaId(), sesion.rol()));
    }

    @PostMapping("/avisos/leidos")
    public ResponseEntity<Void> marcarLeidos(@AuthenticationPrincipal Jwt jwt) {
        SesionActual sesion = SesionActual.de(jwt);
        solicitudService.marcarAvisosLeidos(SolicitudService.destinatarioDe(sesion.cuentaId(), sesion.rol()));
        return ResponseEntity.noContent().build();
    }
}
