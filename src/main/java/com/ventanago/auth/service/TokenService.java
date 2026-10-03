package com.ventanago.auth.service;

import com.ventanago.auth.repository.entity.Cuenta;
import com.ventanago.auth.service.dto.AuthDtos.CuentaDto;
import com.ventanago.auth.service.dto.AuthDtos.SesionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.duracion-horas:12}")
    private long duracionHoras;

    public SesionDto crearSesion(Cuenta cuenta) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(Duration.ofHours(duracionHoras));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ventanago")
                .subject(String.valueOf(cuenta.getCuentaId()))
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim("email", cuenta.getEmail())
                .claim("rol", cuenta.getRol().name())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new SesionDto(token, expira.toEpochMilli(), aDto(cuenta));
    }

    public static CuentaDto aDto(Cuenta cuenta) {
        return new CuentaDto(cuenta.getCuentaId(), cuenta.getEmail(), cuenta.getNombre(), cuenta.getRol().name());
    }
}
