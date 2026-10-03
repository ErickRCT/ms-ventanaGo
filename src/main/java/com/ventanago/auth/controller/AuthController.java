package com.ventanago.auth.controller;

import com.ventanago.auth.service.AuthService;
import com.ventanago.auth.service.TokenService;
import com.ventanago.auth.service.dto.AuthDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Administradores y proveedores. */
    @PostMapping("/login")
    public ResponseEntity<SesionDto> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.loginConPassword(request.email(), request.password()));
    }

    /** Clientes: pide un código de acceso al correo. */
    @PostMapping("/codigo")
    public ResponseEntity<Void> solicitarCodigo(@RequestBody SolicitarCodigoRequest request) {
        authService.solicitarCodigo(request.email());
        return ResponseEntity.noContent().build();
    }

    /** Clientes: entra con el código recibido. */
    @PostMapping("/codigo/verificar")
    public ResponseEntity<SesionDto> verificarCodigo(@RequestBody VerificarCodigoRequest request) {
        return ResponseEntity.ok(authService.verificarCodigo(request.email(), request.codigo()));
    }

    /** Clientes: entra con el ID token de Google. */
    @PostMapping("/google")
    public ResponseEntity<SesionDto> google(@RequestBody GoogleRequest request) {
        return ResponseEntity.ok(authService.loginConGoogle(request.idToken()));
    }

    /** Cuenta de la sesión actual; sirve al front para comprobar que el token sigue siendo válido. */
    @GetMapping("/yo")
    public ResponseEntity<CuentaDto> yo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(TokenService.aDto(authService.cuentaActual(Long.valueOf(jwt.getSubject()))));
    }
}
