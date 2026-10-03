package com.ventanago.auth.service;

import com.ventanago.auth.repository.CuentaRepository;
import com.ventanago.auth.repository.entity.Cuenta;
import com.ventanago.auth.repository.entity.Rol;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Atajo SOLO para desarrollo: entrar con usuario "admin", "cliente" o "proveedor" y una contraseña simple.
 * Se activa definiendo app.dev-login.password (en application-local.properties); sin ella no hace nada.
 * Las cuentas se crean al primer ingreso con correos @dev.local y sin contraseña propia,
 * así que no sirven para entrar cuando el atajo está apagado.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginDesarrollo {

    private static final Map<String, Rol> USUARIOS = Map.of(
            "admin", Rol.ADMIN,
            "cliente", Rol.CLIENTE,
            "proveedor", Rol.PROVEEDOR);

    private final CuentaRepository cuentaRepository;

    @Value("${app.dev-login.password:}")
    private String password;

    @PostConstruct
    void avisar() {
        if (!password.isEmpty()) {
            log.warn("Login de desarrollo ACTIVO: usuarios admin / cliente / proveedor. Nunca definas app.dev-login.password en producción.");
        }
    }

    public Optional<Cuenta> intentar(String usuario, String passwordRecibida) {
        if (password.isEmpty() || !password.equals(passwordRecibida)) return Optional.empty();
        Rol rol = USUARIOS.get(usuario);
        if (rol == null) return Optional.empty();
        String email = usuario + "@dev.local";
        return Optional.of(cuentaRepository.findByEmail(email).orElseGet(() -> {
            Cuenta cuenta = new Cuenta();
            cuenta.setEmail(email);
            cuenta.setNombre(usuario.substring(0, 1).toUpperCase() + usuario.substring(1) + " (desarrollo)");
            cuenta.setRol(rol);
            return cuentaRepository.save(cuenta);
        }));
    }
}
