package com.ventanago.auth;

import com.ventanago.auth.repository.entity.Rol;
import org.springframework.security.oauth2.jwt.Jwt;

/** Datos de la cuenta que hace la petición, leídos del token ya validado por Spring Security. */
public record SesionActual(Long cuentaId, Rol rol) {

    public static SesionActual de(Jwt jwt) {
        return new SesionActual(Long.valueOf(jwt.getSubject()), Rol.valueOf(jwt.getClaimAsString("rol")));
    }
}
