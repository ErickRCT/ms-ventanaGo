package com.ventanago.auth;

import com.ventanago.auth.repository.CuentaRepository;
import com.ventanago.auth.repository.entity.Cuenta;
import com.ventanago.auth.repository.entity.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Crea las cuentas con contraseña indicadas por configuración (variables de entorno), si aún no existen.
 * El administrador nunca se registra desde la app: se define aquí, fuera del código.
 *   APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD
 *   APP_PROVEEDOR_DEMO_EMAIL / APP_PROVEEDOR_DEMO_PASSWORD (opcional, para probar el panel de proveedor)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InicializadorCuentas implements CommandLineRunner {

    private final CuentaRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;
    @Value("${app.admin.password:}")
    private String adminPassword;
    @Value("${app.proveedor-demo.email:}")
    private String proveedorEmail;
    @Value("${app.proveedor-demo.password:}")
    private String proveedorPassword;

    @Override
    public void run(String... args) {
        crearSiFalta(adminEmail, adminPassword, Rol.ADMIN, "Administrador");
        crearSiFalta(proveedorEmail, proveedorPassword, Rol.PROVEEDOR, "Proveedor de prueba");
        if (!cuentaRepository.existsByRol(Rol.ADMIN)) {
            log.warn("No hay ninguna cuenta de administrador. Define APP_ADMIN_EMAIL y APP_ADMIN_PASSWORD y reinicia.");
        }
    }

    private void crearSiFalta(String email, String password, Rol rol, String nombre) {
        if (email.isBlank() || password.isBlank()) return;
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        if (cuentaRepository.findByEmail(normalizado).isPresent()) return;
        if (password.length() < 10) {
            log.error("La contraseña de {} debe tener al menos 10 caracteres; no se creó la cuenta.", normalizado);
            return;
        }
        Cuenta cuenta = new Cuenta();
        cuenta.setEmail(normalizado);
        cuenta.setNombre(nombre);
        cuenta.setRol(rol);
        cuenta.setHashPassword(passwordEncoder.encode(password));
        cuentaRepository.save(cuenta);
        log.info("Cuenta {} creada para {}", rol, normalizado);
    }
}
