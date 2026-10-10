package com.ventanago.proveedor.service;

import com.ventanago.auth.repository.CuentaRepository;
import com.ventanago.auth.repository.entity.Cuenta;
import com.ventanago.auth.repository.entity.Rol;
import com.ventanago.comuna.repository.ComunaRepository;
import com.ventanago.comuna.repository.entity.Comuna;
import com.ventanago.proveedor.repository.PerfilProveedorRepository;
import com.ventanago.proveedor.repository.entity.PerfilProveedor;
import com.ventanago.proveedor.service.dto.ProveedorDtos.*;
import com.ventanago.solicitud.repository.OfertaRepository;
import com.ventanago.solicitud.repository.ValoracionRepository;
import com.ventanago.solicitud.repository.entity.Solicitud.Servicio;
import com.ventanago.solicitud.repository.entity.Valoracion;
import com.ventanago.solicitud.service.dto.SolicitudDtos.ContactoDto;
import com.ventanago.solicitud.service.dto.SolicitudDtos.ValoracionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Perfiles de proveedores: búsqueda por zona y servicio, ficha pública con valoraciones y edición del propio perfil. */
@Service
@RequiredArgsConstructor
public class ProveedorService {

    private static final int MAX_COMUNAS = 60;

    private final PerfilProveedorRepository perfilRepository;
    private final CuentaRepository cuentaRepository;
    private final ComunaRepository comunaRepository;
    private final ValoracionRepository valoracionRepository;
    private final OfertaRepository ofertaRepository;

    /** Proveedores con perfil que atienden la comuna y el servicio (ambos opcionales). Verificados y mejor evaluados primero. */
    @Transactional(readOnly = true)
    public List<ProveedorResumenDto> listar(Long comunaId, String servicio) {
        Set<Servicio> servicios = servicio == null || servicio.isBlank() ? Set.of() : Set.of(parsearServicio(servicio));
        List<Long> ids = perfilRepository.findAll().stream()
                .filter(p -> p.getCuenta().isActivo() && atiende(p, comunaId, servicios))
                .map(PerfilProveedor::getCuentaId)
                .toList();
        return resumenes(ids).values().stream()
                .sorted(Comparator.comparing(ProveedorResumenDto::verificado).reversed()
                        .thenComparing(r -> r.promedio() == null ? 0 : -r.promedio())
                        .thenComparing(ProveedorResumenDto::nombre))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProveedorDetalleDto detalle(Long cuentaId) {
        ProveedorResumenDto resumen = resumenes(List.of(cuentaId)).get(cuentaId);
        if (resumen == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el proveedor.");
        List<ValoracionDto> valoraciones = valoracionRepository.findTop30ByProveedorCuentaIdOrderByFechaDesc(cuentaId).stream()
                .map(ProveedorService::aDto)
                .toList();
        return new ProveedorDetalleDto(resumen, valoraciones);
    }

    /**
     * Resumen público de cada proveedor. Los que aún no completan su perfil aparecen con el nombre de su cuenta,
     * para que sus ofertas se puedan mostrar igual.
     */
    @Transactional(readOnly = true)
    public Map<Long, ProveedorResumenDto> resumenes(Collection<Long> cuentaIds) {
        if (cuentaIds.isEmpty()) return Map.of();
        Map<Long, PerfilProveedor> perfiles = perfilRepository.findAllById(cuentaIds).stream()
                .collect(Collectors.toMap(PerfilProveedor::getCuentaId, Function.identity()));
        Map<Long, Cuenta> cuentas = cuentaRepository.findAllById(cuentaIds).stream()
                .collect(Collectors.toMap(Cuenta::getCuentaId, Function.identity()));
        Map<Long, String> comunas = nombresComunas(perfiles.values().stream().flatMap(p -> p.getComunas().stream()).collect(Collectors.toSet()));
        Map<Long, Object[]> notas = valoracionRepository.resumenPorProveedor().stream()
                .collect(Collectors.toMap(f -> (Long) f[0], Function.identity()));
        Map<Long, Long> trabajos = ofertaRepository.trabajosPorProveedor().stream()
                .collect(Collectors.toMap(f -> (Long) f[0], f -> (Long) f[1]));

        Map<Long, ProveedorResumenDto> resultado = new LinkedHashMap<>();
        for (Long id : cuentaIds) {
            Cuenta cuenta = cuentas.get(id);
            if (cuenta == null) continue;
            PerfilProveedor p = perfiles.get(id);
            Object[] nota = notas.get(id);
            Double promedio = nota == null ? null : Math.round(((Number) nota[1]).doubleValue() * 10) / 10.0;
            long cantidad = nota == null ? 0 : ((Number) nota[2]).longValue();
            resultado.put(id, new ProveedorResumenDto(
                    id,
                    p != null ? p.getNombreComercial() : Objects.requireNonNullElse(cuenta.getNombre(), cuenta.getEmail()),
                    p != null ? p.getDescripcion() : null,
                    p != null && p.isVerificado(),
                    p != null ? p.getAniosExperiencia() : 0,
                    promedio, cantidad, trabajos.getOrDefault(id, 0L),
                    p == null ? List.of() : p.getServicios().stream().map(Enum::name).toList(),
                    p == null ? List.of() : p.getComunas().stream()
                            .filter(comunas::containsKey)
                            .map(c -> new ComunaNombreDto(c, comunas.get(c)))
                            .sorted(Comparator.comparing(ComunaNombreDto::nombre))
                            .toList()));
        }
        return resultado;
    }

    /** Datos de contacto del proveedor; el cliente los recibe al elegir su oferta. */
    @Transactional(readOnly = true)
    public ContactoDto contacto(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow();
        Optional<PerfilProveedor> perfil = perfilRepository.findById(cuentaId);
        return new ContactoDto(
                perfil.map(PerfilProveedor::getNombreComercial).orElse(Objects.requireNonNullElse(cuenta.getNombre(), "")),
                cuenta.getEmail(),
                perfil.map(PerfilProveedor::getTelefono).filter(t -> !t.isBlank()).orElse(Objects.requireNonNullElse(cuenta.getTelefono(), "")),
                "");
    }

    /** Perfiles de todos los proveedores, para decidir a quién le llega cada solicitud. */
    @Transactional(readOnly = true)
    public Map<Long, PerfilProveedor> perfiles() {
        return perfilRepository.findAll().stream().collect(Collectors.toMap(PerfilProveedor::getCuentaId, Function.identity()));
    }

    /**
     * Un perfil sin comunas o sin servicios no filtra por ese criterio (así el proveedor que aún no completa su
     * perfil sigue recibiendo todas las solicitudes, como antes).
     */
    public static boolean atiende(PerfilProveedor p, Long comunaId, Set<Servicio> servicios) {
        boolean comunaOk = comunaId == null || p.getComunas().isEmpty() || p.getComunas().contains(comunaId);
        boolean servicioOk = servicios.isEmpty() || p.getServicios().isEmpty() || servicios.stream().anyMatch(p.getServicios()::contains);
        return comunaOk && servicioOk;
    }

    // ---------- Perfil propio ----------

    @Transactional(readOnly = true)
    public PerfilDto perfil(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow();
        return perfilRepository.findById(cuentaId)
                .map(this::aDto)
                .orElseGet(() -> new PerfilDto(Objects.requireNonNullElse(cuenta.getNombre(), ""), "",
                        Objects.requireNonNullElse(cuenta.getTelefono(), ""), 0, false, List.of(), List.of()));
    }

    @Transactional
    public PerfilDto guardarPerfil(Long cuentaId, PerfilRequest datos) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId).orElseThrow();
        if (cuenta.getRol() != Rol.PROVEEDOR) throw error("Solo las cuentas de proveedor tienen perfil.");
        String nombre = recortar(datos.nombreComercial(), 255);
        if (nombre.isEmpty()) throw error("Ingresa el nombre de tu empresa o el tuyo.");
        if (datos.aniosExperiencia() < 0 || datos.aniosExperiencia() > 80) throw error("Los años de experiencia deben estar entre 0 y 80.");
        Set<Servicio> servicios = new LinkedHashSet<>();
        for (String s : datos.servicios() == null ? List.<String>of() : datos.servicios()) servicios.add(parsearServicio(s));
        if (servicios.isEmpty()) throw error("Elige al menos un servicio que prestas.");
        Set<Long> comunas = new LinkedHashSet<>(datos.comunas() == null ? List.of() : datos.comunas());
        if (comunas.isEmpty()) throw error("Elige al menos una comuna donde trabajas.");
        if (comunas.size() > MAX_COMUNAS) throw error("Puedes elegir hasta " + MAX_COMUNAS + " comunas.");
        if (comunaRepository.findAllById(comunas).size() != comunas.size()) throw error("Alguna de las comunas no existe.");

        PerfilProveedor perfil = perfilRepository.findById(cuentaId).orElseGet(() -> {
            PerfilProveedor nuevo = new PerfilProveedor();
            nuevo.setCuenta(cuenta);
            return nuevo;
        });
        perfil.setNombreComercial(nombre);
        perfil.setDescripcion(recortar(datos.descripcion(), 1000));
        perfil.setTelefono(recortar(datos.telefono(), 50));
        perfil.setAniosExperiencia(datos.aniosExperiencia());
        perfil.setServicios(servicios);
        perfil.setComunas(comunas);
        return aDto(perfilRepository.save(perfil));
    }

    @Transactional
    public void verificar(Long cuentaId, boolean verificado) {
        PerfilProveedor perfil = perfilRepository.findById(cuentaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El proveedor aún no completa su perfil."));
        perfil.setVerificado(verificado);
    }

    // ---------- Conversión ----------

    private PerfilDto aDto(PerfilProveedor p) {
        Map<Long, String> comunas = nombresComunas(p.getComunas());
        return new PerfilDto(p.getNombreComercial(), Objects.requireNonNullElse(p.getDescripcion(), ""),
                Objects.requireNonNullElse(p.getTelefono(), ""), p.getAniosExperiencia(), p.isVerificado(),
                p.getServicios().stream().map(Enum::name).toList(),
                p.getComunas().stream().filter(comunas::containsKey).map(c -> new ComunaNombreDto(c, comunas.get(c))).toList());
    }

    private Map<Long, String> nombresComunas(Set<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return comunaRepository.findAllById(ids).stream().collect(Collectors.toMap(Comuna::getComunaId, Comuna::getNombre));
    }

    /** Solo el primer nombre del cliente, para no exponer sus datos en la ficha pública. */
    public static ValoracionDto aDto(Valoracion v) {
        String nombre = v.getSolicitud().getContactoNombre();
        String autor = nombre == null || nombre.isBlank() ? "Cliente" : nombre.trim().split("\\s+")[0];
        return new ValoracionDto(v.getEstrellas(), v.getComentario(),
                v.getFecha().atZone(ZoneId.systemDefault()).toOffsetDateTime().toString(), autor);
    }

    private static Servicio parsearServicio(String s) {
        try {
            return Servicio.valueOf(s);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw error("Servicio desconocido: " + s);
        }
    }

    private static String recortar(String texto, int maximo) {
        if (texto == null) return "";
        String limpio = texto.trim();
        return limpio.length() > maximo ? limpio.substring(0, maximo) : limpio;
    }

    private static ResponseStatusException error(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
