package com.ventanago.solicitud.controller;

import com.ventanago.auth.SesionActual;
import com.ventanago.solicitud.repository.entity.FotoSolicitud;
import com.ventanago.solicitud.service.SolicitudService;
import com.ventanago.solicitud.service.dto.SolicitudDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
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

    /** Proveedor: envía su oferta (acepta con precio, propone cambios o no toma el trabajo). */
    @PostMapping("/solicitudes/{numero}/respuesta")
    public SolicitudDto responder(@AuthenticationPrincipal Jwt jwt, @PathVariable Long numero, @RequestBody RespuestaRequest respuesta) {
        SesionActual sesion = SesionActual.de(jwt);
        return solicitudService.responder(numero, sesion.cuentaId(), sesion.rol(), respuesta);
    }

    /** Cliente: elige una de las ofertas. */
    @PostMapping("/solicitudes/{numero}/ofertas/{ofertaId}/elegir")
    public SolicitudDto elegir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long numero, @PathVariable Long ofertaId) {
        SesionActual sesion = SesionActual.de(jwt);
        return solicitudService.elegirOferta(numero, ofertaId, sesion.cuentaId(), sesion.rol());
    }

    @PostMapping("/solicitudes/{numero}/cancelar")
    public SolicitudDto cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long numero) {
        SesionActual sesion = SesionActual.de(jwt);
        return solicitudService.cancelar(numero, sesion.cuentaId(), sesion.rol());
    }

    /** Cliente: valora al proveedor elegido; la solicitud queda terminada. */
    @PostMapping("/solicitudes/{numero}/valoracion")
    public SolicitudDto valorar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long numero, @RequestBody ValoracionRequest datos) {
        SesionActual sesion = SesionActual.de(jwt);
        return solicitudService.valorar(numero, sesion.cuentaId(), sesion.rol(), datos);
    }

    @GetMapping("/solicitudes/{numero}/fotos/{fotoId}")
    public ResponseEntity<byte[]> foto(@AuthenticationPrincipal Jwt jwt, @PathVariable Long numero, @PathVariable Long fotoId) {
        SesionActual sesion = SesionActual.de(jwt);
        FotoSolicitud foto = solicitudService.foto(numero, fotoId, sesion.cuentaId(), sesion.rol());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.getTipoContenido()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePrivate())
                .body(foto.getDatos());
    }

    /** Público: rango de precio por m² según las ofertas ya enviadas. */
    @GetMapping("/precios/referencia")
    public PrecioReferenciaDto precioReferencia() {
        return solicitudService.precioReferencia();
    }

    // ---------- Chat de cada oferta (cliente y proveedor) ----------

    @GetMapping("/ofertas/{ofertaId}/mensajes")
    public List<MensajeDto> mensajes(@AuthenticationPrincipal Jwt jwt, @PathVariable Long ofertaId) {
        SesionActual sesion = SesionActual.de(jwt);
        return solicitudService.mensajes(ofertaId, sesion.cuentaId(), sesion.rol());
    }

    @PostMapping("/ofertas/{ofertaId}/mensajes")
    public MensajeDto enviarMensaje(@AuthenticationPrincipal Jwt jwt, @PathVariable Long ofertaId, @RequestBody NuevoMensajeRequest datos) {
        SesionActual sesion = SesionActual.de(jwt);
        return solicitudService.enviarMensaje(ofertaId, sesion.cuentaId(), sesion.rol(), datos.texto());
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
