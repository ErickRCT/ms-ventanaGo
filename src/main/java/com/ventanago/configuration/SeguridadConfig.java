package com.ventanago.configuration;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * API sin sesión de servidor: cada petición trae "Authorization: Bearer <token>" emitido por /auth.
 * Lo que el cliente usa sin cuenta (catálogo y precio) es público; el resto es solo del administrador
 * hasta que existan los endpoints de clientes y proveedores.
 */
@Slf4j
@Configuration
public class SeguridadConfig {

    /** Lectura del catálogo con que el cliente diseña su ventana. */
    private static final String[] CATALOGO_PUBLICO = {
            "/serie", "/serie/**", "/pauta", "/pauta/**", "/tipo-pauta", "/tipo-pauta/**",
            "/color", "/color/**", "/vidrio", "/vidrio/**", "/region", "/region/**", "/comuna", "/comuna/**",
    };

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/codigo", "/auth/codigo/verificar", "/auth/google").permitAll()
                        .requestMatchers(HttpMethod.GET, CATALOGO_PUBLICO).permitAll()
                        .requestMatchers(HttpMethod.POST, "/ventana/cotizar").permitAll()
                        .requestMatchers("/auth/yo").authenticated()
                        // Carrito y envío de solicitudes: el cliente (y el administrador, que puede entrar a todo).
                        .requestMatchers("/carrito", "/carrito/**").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/solicitudes").hasAnyRole("CLIENTE", "ADMIN")
                        // Responder: proveedores y administrador.
                        .requestMatchers(HttpMethod.POST, "/solicitudes/*/respuesta").hasAnyRole("PROVEEDOR", "ADMIN")
                        // Listado y avisos: cada rol ve lo suyo (se filtra en el servicio).
                        .requestMatchers(HttpMethod.GET, "/solicitudes").authenticated()
                        .requestMatchers("/avisos", "/avisos/**").authenticated()
                        .anyRequest().hasRole("ADMIN"))
                .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorRoles())));
        return http.build();
    }

    /** El claim "rol" del token pasa a ser la autoridad ROLE_<rol>. */
    private JwtAuthenticationConverter convertidorRoles() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName("rol");
        autoridades.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(autoridades);
        return convertidor;
    }

    @Bean
    public SecretKey claveJwt(@Value("${app.jwt.secret:}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // Sin secreto configurado se usa uno al azar: sirve en local, pero las sesiones se pierden al reiniciar.
            log.warn("APP_JWT_SECRET no está definido o tiene menos de 32 caracteres; se usa una clave temporal.");
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey claveJwt) {
        return NimbusJwtDecoder.withSecretKey(claveJwt).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
