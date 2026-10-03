package com.ventanago.auth.service;

import com.ventanago.auth.repository.CodigoAccesoRepository;
import com.ventanago.auth.repository.CuentaRepository;
import com.ventanago.auth.repository.entity.CodigoAcceso;
import com.ventanago.auth.repository.entity.Cuenta;
import com.ventanago.auth.repository.entity.Rol;
import com.ventanago.auth.service.dto.AuthDtos.SesionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MINUTOS_VIGENCIA_CODIGO = 10;
    private static final int MAX_INTENTOS_CODIGO = 5;
    private static final int MAX_CODIGOS_POR_VENTANA = 3;
    private static final int MINUTOS_VENTANA_CODIGOS = 15;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Set<String> EMISORES_GOOGLE = Set.of("accounts.google.com", "https://accounts.google.com");
    private static final String CERTIFICADOS_GOOGLE = "https://www.googleapis.com/oauth2/v3/certs";

    private final CuentaRepository cuentaRepository;
    private final CodigoAccesoRepository codigoAccesoRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EnvioCodigo envioCodigo;
    private final LoginDesarrollo loginDesarrollo;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.google.client-id:}")
    private String googleClientId;

    private JwtDecoder decodificadorGoogle;

    /** Administradores y proveedores: correo y contraseña. Un mensaje único para no revelar qué falló. */
    public SesionDto loginConPassword(String email, String password) {
        Optional<Cuenta> desarrollo = loginDesarrollo.intentar(normalizar(email), password);
        if (desarrollo.isPresent()) return tokenService.crearSesion(desarrollo.get());
        Cuenta cuenta = cuentaRepository.findByEmail(normalizar(email))
                .filter(Cuenta::isActivo)
                .filter(c -> c.getHashPassword() != null && password != null && passwordEncoder.matches(password, c.getHashPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos."));
        return tokenService.crearSesion(cuenta);
    }

    /** Clientes: genera un código de 6 dígitos y lo envía al correo. */
    @Transactional
    public void solicitarCodigo(String emailRecibido) {
        String email = normalizar(emailRecibido);
        if (!EMAIL.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un correo válido.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (codigoAccesoRepository.countByEmailAndCreadoAfter(email, ahora.minusMinutes(MINUTOS_VENTANA_CODIGOS)) >= MAX_CODIGOS_POR_VENTANA) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Pediste varios códigos seguidos. Espera unos minutos.");
        }
        // Las cuentas de administrador y proveedor no entran con código, solo con contraseña.
        if (cuentaRepository.findByEmail(email).map(c -> c.getRol() != Rol.CLIENTE).orElse(false)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta cuenta entra con correo y contraseña.");
        }
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        CodigoAcceso nuevo = new CodigoAcceso();
        nuevo.setEmail(email);
        nuevo.setHashCodigo(passwordEncoder.encode(codigo));
        nuevo.setCreado(ahora);
        nuevo.setExpira(ahora.plusMinutes(MINUTOS_VIGENCIA_CODIGO));
        codigoAccesoRepository.save(nuevo);
        envioCodigo.enviar(email, codigo, MINUTOS_VIGENCIA_CODIGO);
    }

    /** Clientes: valida el último código pedido y, si no existe la cuenta, la crea. */
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public SesionDto verificarCodigo(String emailRecibido, String codigo) {
        String email = normalizar(emailRecibido);
        CodigoAcceso pendiente = codigoAccesoRepository.findFirstByEmailAndUsadoFalseOrderByCreadoDesc(email)
                .filter(c -> c.getExpira().isAfter(LocalDateTime.now()) && c.getIntentos() < MAX_INTENTOS_CODIGO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El código venció. Pide uno nuevo."));
        pendiente.setIntentos(pendiente.getIntentos() + 1);
        if (codigo == null || !passwordEncoder.matches(codigo.trim(), pendiente.getHashCodigo())) {
            codigoAccesoRepository.save(pendiente);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código incorrecto.");
        }
        pendiente.setUsado(true);
        codigoAccesoRepository.save(pendiente);
        return tokenService.crearSesion(cuentaDeCliente(email, null, null));
    }

    /** Clientes: valida el ID token que entrega "Iniciar sesión con Google" en el navegador. */
    @Transactional
    public SesionDto loginConGoogle(String idToken) {
        if (googleClientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El inicio de sesión con Google no está configurado.");
        }
        Jwt jwt;
        try {
            jwt = decodificadorGoogle().decode(idToken);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo validar la cuenta de Google.");
        }
        if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El correo de Google no está verificado.");
        }
        String sub = jwt.getSubject();
        Cuenta cuenta = cuentaRepository.findByGoogleSub(sub)
                .orElseGet(() -> cuentaDeCliente(normalizar(jwt.getClaimAsString("email")), sub, jwt.getClaimAsString("name")));
        return tokenService.crearSesion(cuenta);
    }

    public Cuenta cuentaActual(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .filter(Cuenta::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private Cuenta cuentaDeCliente(String email, String googleSub, String nombre) {
        Cuenta cuenta = cuentaRepository.findByEmail(email).orElseGet(() -> {
            Cuenta nueva = new Cuenta();
            nueva.setEmail(email);
            nueva.setRol(Rol.CLIENTE);
            return nueva;
        });
        // Un administrador o proveedor nunca entra por las vías de cliente.
        if (cuenta.getRol() != Rol.CLIENTE || !cuenta.isActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta cuenta entra con correo y contraseña.");
        }
        if (googleSub != null && cuenta.getGoogleSub() == null) cuenta.setGoogleSub(googleSub);
        if (nombre != null && cuenta.getNombre() == null) cuenta.setNombre(nombre);
        return cuentaRepository.save(cuenta);
    }

    private JwtDecoder decodificadorGoogle() {
        if (decodificadorGoogle == null) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(CERTIFICADOS_GOOGLE).build();
            OAuth2TokenValidator<Jwt> emisorYAudiencia = jwt -> {
                boolean emisorOk = jwt.getIssuer() != null && EMISORES_GOOGLE.contains(jwt.getIssuer().toString());
                boolean audienciaOk = jwt.getAudience() != null && jwt.getAudience().contains(googleClientId);
                return emisorOk && audienciaOk
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token de Google no emitido para VentanaGo", null));
            };
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), emisorYAudiencia));
            decodificadorGoogle = decoder;
        }
        return decodificadorGoogle;
    }

    static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
