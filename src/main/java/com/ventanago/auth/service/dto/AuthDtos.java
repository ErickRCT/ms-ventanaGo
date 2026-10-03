package com.ventanago.auth.service.dto;

/** Cuerpos de las peticiones y respuestas de /auth. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(String email, String password) {
    }

    public record SolicitarCodigoRequest(String email) {
    }

    public record VerificarCodigoRequest(String email, String codigo) {
    }

    public record GoogleRequest(String idToken) {
    }

    public record CuentaDto(Long cuentaId, String email, String nombre, String rol) {
    }

    /** Token para el header Authorization y los datos de la cuenta para el front. */
    public record SesionDto(String token, long expiraEn, CuentaDto cuenta) {
    }
}
