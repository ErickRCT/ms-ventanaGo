package com.ventanago.notificacion.controller;

import com.ventanago.auth.SesionActual;
import com.ventanago.notificacion.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Teléfonos que reciben las notificaciones push de la cuenta. */
@RestController
@RequiredArgsConstructor
public class DispositivoController {

    public record TokenRequest(String token) {
    }

    private final PushService pushService;

    @PostMapping("/dispositivos")
    public ResponseEntity<Void> registrar(@AuthenticationPrincipal Jwt jwt, @RequestBody TokenRequest datos) {
        pushService.registrar(SesionActual.de(jwt).cuentaId(), validar(datos));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dispositivos/quitar")
    public ResponseEntity<Void> quitar(@AuthenticationPrincipal Jwt jwt, @RequestBody TokenRequest datos) {
        pushService.quitar(SesionActual.de(jwt).cuentaId(), validar(datos));
        return ResponseEntity.noContent().build();
    }

    private static String validar(TokenRequest datos) {
        String token = datos == null || datos.token() == null ? "" : datos.token().trim();
        if (token.isEmpty() || token.length() > 512) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de notificaciones no válido.");
        }
        return token;
    }
}
