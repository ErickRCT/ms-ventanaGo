package com.ventanago.proveedor;

import com.ventanago.auth.repository.CuentaRepository;
import com.ventanago.auth.repository.entity.Cuenta;
import com.ventanago.auth.repository.entity.Rol;
import com.ventanago.comuna.repository.ComunaRepository;
import com.ventanago.comuna.repository.entity.Comuna;
import com.ventanago.proveedor.repository.PerfilProveedorRepository;
import com.ventanago.proveedor.repository.entity.PerfilProveedor;
import com.ventanago.solicitud.repository.OfertaRepository;
import com.ventanago.solicitud.repository.SolicitudRepository;
import com.ventanago.solicitud.repository.ValoracionRepository;
import com.ventanago.solicitud.repository.entity.Oferta;
import com.ventanago.solicitud.repository.entity.Solicitud;
import com.ventanago.solicitud.repository.entity.Solicitud.Servicio;
import com.ventanago.solicitud.repository.entity.Valoracion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Proveedores de prueba para probar el mercado (ofertas, comparación, chat y valoraciones) sin empresas reales.
 * Se activa definiendo app.proveedores-prueba.password (APP_PROVEEDORES_PRUEBA_PASSWORD), de al menos 10 caracteres:
 * todas las cuentas usan esa contraseña. Cada uno trae trabajos terminados con valoraciones de un cliente de prueba,
 * para que su ficha no aparezca vacía. Las cuentas usan el dominio @prueba.ventanago.cl y "(prueba)" en el nombre.
 * Solo se crean las que faltan, así que se puede dejar activo.
 */
@Slf4j
@Order(10)
@Component
public class ProveedoresPrueba implements CommandLineRunner {

    public static final String DOMINIO = "@prueba.ventanago.cl";

    /** [estrellas, comentario] de los trabajos terminados que trae cada proveedor. */
    private record Trabajo(int estrellas, String comentario, long total) {
    }

    private record Datos(String usuario, String nombre, String descripcion, String telefono, int anios, boolean verificado,
                         List<Servicio> servicios, List<String> comunas, List<Trabajo> trabajos) {
    }

    private static final List<String> SANTIAGO_CENTRO = List.of("Santiago", "Providencia", "Ñuñoa", "Las Condes", "Vitacura", "La Reina", "Lo Barnechea");
    private static final List<String> SANTIAGO_PONIENTE = List.of("Maipú", "Cerrillos", "Estación Central", "Pudahuel", "Quinta Normal", "Lo Prado", "Santiago");
    private static final List<String> SANTIAGO_SUR = List.of("La Florida", "Puente Alto", "La Granja", "Peñalolén", "Macul", "San Joaquín", "La Pintana");

    private static final List<Datos> PROVEEDORES = List.of(
            new Datos("andes", "Vidrios y Aluminios Los Andes (prueba)",
                    "Fabricamos ventanas de aluminio y PVC a medida, con termopanel. Instalación con garantía de un año.",
                    "+56 9 1111 0001", 15, true,
                    List.of(Servicio.FABRICACION, Servicio.INSTALACION, Servicio.MEDICION, Servicio.CAMBIO_VIDRIO), SANTIAGO_CENTRO,
                    List.of(new Trabajo(5, "Muy puntuales y prolijos con la instalación.", 640_000),
                            new Trabajo(5, "Las ventanas quedaron perfectas, sin filtraciones.", 1_150_000),
                            new Trabajo(4, "Buen trabajo, se demoraron un par de días más de lo dicho.", 380_000))),
            new Datos("maipu", "Ventanas Maipú Express (prueba)",
                    "Ventanas de aluminio línea 20 y 25 listas en 5 días hábiles. Incluye flete en la zona poniente.",
                    "+56 9 1111 0002", 8, true,
                    List.of(Servicio.FABRICACION, Servicio.INSTALACION, Servicio.FLETE), SANTIAGO_PONIENTE,
                    List.of(new Trabajo(4, "Buen precio y rápidos.", 420_000),
                            new Trabajo(5, "Recomendados, cumplieron el plazo.", 510_000))),
            new Datos("raul", "Don Raúl, maestro ventanero (prueba)",
                    "Más de 20 años arreglando ventanas: correderas que no corren, burletes, chapas, cambio de vidrios.",
                    "+56 9 1111 0003", 22, false,
                    List.of(Servicio.INSTALACION, Servicio.REPARACION, Servicio.MANTENCION, Servicio.CAMBIO_VIDRIO), SANTIAGO_SUR,
                    List.of(new Trabajo(5, "Dejó las correderas como nuevas.", 45_000),
                            new Trabajo(3, "Arregló todo, pero llegó tarde.", 30_000))),
            new Datos("cordillera", "Fletes Cordillera (prueba)",
                    "Retiro en fábrica y entrega de ventanas en camioneta acondicionada, con ayudante.",
                    "+56 9 1111 0004", 5, false,
                    List.of(Servicio.FLETE), concatenar(SANTIAGO_CENTRO, SANTIAGO_PONIENTE, SANTIAGO_SUR),
                    List.of()),
            new Datos("costa", "Cristalería Costa Azul (prueba)",
                    "Ventanas, vidrios templados y reparaciones en la Quinta Región. Visita técnica sin costo en Viña.",
                    "+56 9 1111 0005", 12, true,
                    List.of(Servicio.FABRICACION, Servicio.INSTALACION, Servicio.CAMBIO_VIDRIO, Servicio.REPARACION, Servicio.MEDICION),
                    List.of("Viña del Mar", "Valparaíso", "Concón", "Quilpué", "Villa Alemana"),
                    List.of(new Trabajo(5, "Excelente atención y terminaciones.", 890_000))),
            new Datos("biobio", "Termopanel Biobío (prueba)",
                    "Especialistas en termopanel y PVC para el sur: menos frío y menos condensación.",
                    "+56 9 1111 0006", 10, false,
                    List.of(Servicio.FABRICACION, Servicio.INSTALACION, Servicio.MEDICION),
                    List.of("Concepción", "Talcahuano", "San Pedro de la Paz", "Chiguayante", "Hualpén"),
                    List.of(new Trabajo(4, "Buen producto, la instalación fue ordenada.", 1_300_000))));

    private final CuentaRepository cuentaRepository;
    private final PerfilProveedorRepository perfilRepository;
    private final ComunaRepository comunaRepository;
    private final SolicitudRepository solicitudRepository;
    private final OfertaRepository ofertaRepository;
    private final ValoracionRepository valoracionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transaccion;

    @Value("${app.proveedores-prueba.password:}")
    private String password;

    public ProveedoresPrueba(CuentaRepository cuentaRepository, PerfilProveedorRepository perfilRepository,
                             ComunaRepository comunaRepository, SolicitudRepository solicitudRepository,
                             OfertaRepository ofertaRepository, ValoracionRepository valoracionRepository,
                             PasswordEncoder passwordEncoder, PlatformTransactionManager tm) {
        this.cuentaRepository = cuentaRepository;
        this.perfilRepository = perfilRepository;
        this.comunaRepository = comunaRepository;
        this.solicitudRepository = solicitudRepository;
        this.ofertaRepository = ofertaRepository;
        this.valoracionRepository = valoracionRepository;
        this.passwordEncoder = passwordEncoder;
        this.transaccion = new TransactionTemplate(tm);
    }

    @Override
    public void run(String... args) {
        if (password.isBlank()) return;
        if (password.length() < 10) {
            log.error("app.proveedores-prueba.password debe tener al menos 10 caracteres; no se crearon proveedores de prueba.");
            return;
        }
        Integer creados = transaccion.execute(estado -> crear());
        if (creados != null && creados > 0) {
            log.info("{} proveedor(es) de prueba creados: proveedor.<nombre>{} con la contraseña de app.proveedores-prueba.password.", creados, DOMINIO);
        }
    }

    private int crear() {
        Map<String, Long> comunas = comunaRepository.findAll().stream()
                .collect(Collectors.toMap(c -> normalizar(c.getNombre()), Comuna::getComunaId, (a, b) -> a));
        if (comunas.isEmpty()) {
            log.warn("La tabla comuna está vacía: los proveedores de prueba quedan sin zona (recibirán todas las solicitudes).");
        }
        String hash = passwordEncoder.encode(password);
        Cuenta cliente = null;
        int creados = 0;
        for (Datos datos : PROVEEDORES) {
            String email = "proveedor." + datos.usuario() + DOMINIO;
            if (cuentaRepository.findByEmail(email).isPresent()) continue;

            Cuenta cuenta = new Cuenta();
            cuenta.setEmail(email);
            cuenta.setNombre(datos.nombre());
            cuenta.setTelefono(datos.telefono());
            cuenta.setRol(Rol.PROVEEDOR);
            cuenta.setHashPassword(hash);
            cuentaRepository.save(cuenta);

            PerfilProveedor perfil = new PerfilProveedor();
            perfil.setCuenta(cuenta);
            perfil.setNombreComercial(datos.nombre());
            perfil.setDescripcion(datos.descripcion());
            perfil.setTelefono(datos.telefono());
            perfil.setAniosExperiencia(datos.anios());
            perfil.setVerificado(datos.verificado());
            perfil.setServicios(new LinkedHashSet<>(datos.servicios()));
            Set<Long> ids = datos.comunas().stream().map(c -> comunas.get(normalizar(c))).filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (ids.size() < datos.comunas().size()) {
                log.warn("Proveedor de prueba {}: {} de {} comunas no existen en la base.", email, datos.comunas().size() - ids.size(), datos.comunas().size());
            }
            perfil.setComunas(ids);
            perfilRepository.save(perfil);

            if (!datos.trabajos().isEmpty() && cliente == null) cliente = clientePrueba();
            for (Trabajo t : datos.trabajos()) trabajoTerminado(cliente, cuenta, datos, ids, t);
            creados++;
        }
        return creados;
    }

    /** Cliente dueño de los trabajos terminados de prueba. No tiene contraseña: no sirve para entrar. */
    private Cuenta clientePrueba() {
        String email = "cliente" + DOMINIO;
        return cuentaRepository.findByEmail(email).orElseGet(() -> {
            Cuenta c = new Cuenta();
            c.setEmail(email);
            c.setNombre("Cliente de prueba");
            c.setRol(Rol.CLIENTE);
            return cuentaRepository.save(c);
        });
    }

    private void trabajoTerminado(Cuenta cliente, Cuenta proveedor, Datos datos, Set<Long> comunas, Trabajo t) {
        LocalDateTime fecha = LocalDateTime.now().minusDays(20 + new Random().nextInt(160));
        Solicitud s = new Solicitud();
        s.setCliente(cliente);
        s.setFecha(fecha);
        s.setContactoNombre("Cliente de prueba");
        s.setContactoEmail(cliente.getEmail());
        s.setContactoTelefono("");
        s.setContactoDireccion("Dirección de prueba");
        s.setServicios(new LinkedHashSet<>(datos.servicios().subList(0, Math.min(2, datos.servicios().size()))));
        s.setObservaciones("Trabajo de prueba generado automáticamente.");
        s.setComunaId(comunas.stream().findFirst().orElse(null));
        s.setEstado(Solicitud.Estado.TERMINADA);
        s.setRespondidaPor(proveedor);
        s.setFechaRespuesta(fecha.plusDays(1));
        s.setTotal(t.total());
        solicitudRepository.save(s);

        Oferta o = new Oferta();
        o.setSolicitud(s);
        o.setProveedor(proveedor);
        o.setFecha(fecha.plusDays(1));
        o.setTipo(Oferta.Tipo.ACEPTADA);
        o.setEstado(Oferta.Estado.ELEGIDA);
        o.setTotal(t.total());
        o.setPlazoDias(7);
        o.setMensaje("Oferta de prueba.");
        ofertaRepository.save(o);

        Valoracion v = new Valoracion();
        v.setSolicitud(s);
        v.setProveedor(proveedor);
        v.setEstrellas(t.estrellas());
        v.setComentario(t.comentario());
        v.setFecha(fecha.plusDays(12));
        valoracionRepository.save(v);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    @SafeVarargs
    private static List<String> concatenar(List<String>... listas) {
        return Arrays.stream(listas).flatMap(List::stream).distinct().toList();
    }
}
