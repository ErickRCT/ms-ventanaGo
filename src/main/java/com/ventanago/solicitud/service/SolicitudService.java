package com.ventanago.solicitud.service;

import com.ventanago.auth.repository.CuentaRepository;
import com.ventanago.auth.repository.entity.Rol;
import com.ventanago.solicitud.repository.AvisoRepository;
import com.ventanago.solicitud.repository.CarritoItemRepository;
import com.ventanago.solicitud.repository.SolicitudRepository;
import com.ventanago.solicitud.repository.entity.*;
import com.ventanago.solicitud.repository.entity.Solicitud.Estado;
import com.ventanago.solicitud.repository.entity.Solicitud.Servicio;
import com.ventanago.solicitud.service.dto.SolicitudDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Carrito del cliente, solicitudes de cotización y avisos.
 * Mientras las solicitudes no se asignen a un proveedor en particular, todos los proveedores las ven y responden.
 */
@Service
@RequiredArgsConstructor
public class SolicitudService {

    // Mismos límites generales que el front (LIMITES en solicitudes/tipos.ts).
    private static final int MIN_MM = 300;
    private static final int MAX_ANCHO_MM = 9000;
    private static final int MAX_ALTO_MM = 3000;
    private static final int MAX_CANTIDAD = 99;
    private static final int MAX_ITEMS_CARRITO = 50;
    private static final long MAX_PRECIO = 100_000_000L;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final CarritoItemRepository carritoRepository;
    private final SolicitudRepository solicitudRepository;
    private final AvisoRepository avisoRepository;
    private final CuentaRepository cuentaRepository;

    // ---------- Carrito ----------

    @Transactional(readOnly = true)
    public List<ItemVentanaDto> carrito(Long cuentaId) {
        return carritoRepository.findByCuentaCuentaIdOrderByCarritoItemIdAsc(cuentaId).stream()
                .map(item -> aDto(String.valueOf(item.getCarritoItemId()), item.getVentana(), null))
                .toList();
    }

    @Transactional
    public List<ItemVentanaDto> agregarAlCarrito(Long cuentaId, ItemVentanaDto item) {
        if (carritoRepository.findByCuentaCuentaIdOrderByCarritoItemIdAsc(cuentaId).size() >= MAX_ITEMS_CARRITO) {
            throw error(HttpStatus.BAD_REQUEST, "El carrito admite hasta " + MAX_ITEMS_CARRITO + " ventanas distintas.");
        }
        CarritoItem nuevo = new CarritoItem();
        nuevo.setCuenta(cuentaRepository.getReferenceById(cuentaId));
        nuevo.setVentana(validarVentana(item));
        carritoRepository.save(nuevo);
        return carrito(cuentaId);
    }

    @Transactional
    public List<ItemVentanaDto> cambiarCantidad(Long cuentaId, Long itemId, int cantidad) {
        if (cantidad < 1 || cantidad > MAX_CANTIDAD) throw error(HttpStatus.BAD_REQUEST, "La cantidad debe estar entre 1 y " + MAX_CANTIDAD + ".");
        carritoDelCliente(cuentaId, itemId).getVentana().setCantidad(cantidad);
        return carrito(cuentaId);
    }

    @Transactional
    public List<ItemVentanaDto> quitarDelCarrito(Long cuentaId, Long itemId) {
        carritoRepository.delete(carritoDelCliente(cuentaId, itemId));
        carritoRepository.flush();
        return carrito(cuentaId);
    }

    // ---------- Solicitudes ----------

    /** El cliente ve solo las suyas; proveedores y administrador ven todas. */
    @Transactional(readOnly = true)
    public List<SolicitudDto> solicitudes(Long cuentaId, Rol rol) {
        List<Solicitud> solicitudes = rol == Rol.CLIENTE
                ? solicitudRepository.findByClienteCuentaIdOrderByNumeroAsc(cuentaId)
                : solicitudRepository.findAllByOrderByNumeroAsc();
        return solicitudes.stream().map(this::aDto).toList();
    }

    /** Convierte el carrito del cliente en una solicitud pendiente, lo vacía y avisa a los proveedores. */
    @Transactional
    public SolicitudDto enviarSolicitud(Long cuentaId, NuevaSolicitudRequest datos) {
        List<CarritoItem> carrito = carritoRepository.findByCuentaCuentaIdOrderByCarritoItemIdAsc(cuentaId);
        if (carrito.isEmpty()) throw error(HttpStatus.BAD_REQUEST, "El carrito está vacío.");
        Set<Servicio> servicios = validarServicios(datos.servicios());
        ContactoDto contacto = validarContacto(datos.contacto(), servicios);

        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cuentaRepository.getReferenceById(cuentaId));
        solicitud.setFecha(LocalDateTime.now());
        solicitud.setContactoNombre(contacto.nombre());
        solicitud.setContactoEmail(contacto.email());
        solicitud.setContactoTelefono(contacto.telefono());
        solicitud.setContactoDireccion(contacto.direccion());
        solicitud.setServicios(servicios);
        solicitud.setObservaciones(recortar(datos.observaciones(), 2000));
        for (CarritoItem item : carrito) {
            SolicitudItem nuevo = new SolicitudItem();
            nuevo.setSolicitud(solicitud);
            nuevo.setVentana(item.getVentana().copia());
            solicitud.getItems().add(nuevo);
        }
        solicitudRepository.save(solicitud);
        carritoRepository.deleteAll(carrito);

        crearAviso(Aviso.PROVEEDORES, solicitud.getNumero(), "Nueva solicitud N°" + solicitud.getNumero(),
                contacto.nombre() + " solicitó cotizar " + carrito.size() + " tipo(s) de ventana.");
        return aDto(solicitud);
    }

    /** Registra lo que decidió el proveedor y, si corresponde, avisa al cliente. */
    @Transactional
    public SolicitudDto responder(Long numero, Long proveedorId, RespuestaRequest respuesta) {
        Solicitud solicitud = solicitudRepository.findById(numero)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe la solicitud N°" + numero + "."));
        if (solicitud.getEstado() != Estado.PENDIENTE) {
            throw error(HttpStatus.CONFLICT, "La solicitud N°" + numero + " ya fue respondida.");
        }
        Estado estado = parsearEstadoRespuesta(respuesta.estado());
        String mensaje = recortar(respuesta.mensaje(), 2000);
        if (estado != Estado.ACEPTADA && mensaje.isEmpty()) {
            throw error(HttpStatus.BAD_REQUEST, estado == Estado.RECHAZADA ? "Indica el motivo del rechazo." : "Explica al cliente qué modificaste.");
        }

        if (estado != Estado.RECHAZADA) {
            aplicarPrecios(solicitud, estado, respuesta.items());
        }
        solicitud.setEstado(estado);
        solicitud.setMensajeRespuesta(mensaje);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setRespondidaPor(cuentaRepository.getReferenceById(proveedorId));
        solicitud.setTotal(estado == Estado.RECHAZADA ? null : vigentes(solicitud).stream()
                .mapToLong(i -> i.getPrecioUnitario() * i.getVentana().getCantidad()).sum());
        solicitud.setNotificadoEnApp(respuesta.notificarEnApp());
        solicitud.setNotificadoPorCorreo(respuesta.notificarPorCorreo());

        if (respuesta.notificarEnApp()) {
            crearAviso(Aviso.deCuenta(solicitud.getCliente().getCuentaId()), numero,
                    "Cotización N°" + numero + " " + estado.name().toLowerCase(Locale.ROOT), mensaje);
        }
        return aDto(solicitud);
    }

    /**
     * Aceptar: solo se definen precios. Modificar: además cambian medidas o cantidades,
     * y se guarda una copia de lo que pidió el cliente para mostrarle la diferencia.
     */
    private void aplicarPrecios(Solicitud solicitud, Estado estado, List<ItemVentanaDto> recibidos) {
        List<SolicitudItem> actuales = vigentes(solicitud);
        Map<String, ItemVentanaDto> porId = (recibidos == null ? List.<ItemVentanaDto>of() : recibidos).stream()
                .filter(i -> i.id() != null)
                .collect(Collectors.toMap(ItemVentanaDto::id, Function.identity(), (a, b) -> a));
        if (porId.size() != actuales.size() || !actuales.stream().allMatch(i -> porId.containsKey(String.valueOf(i.getSolicitudItemId())))) {
            throw error(HttpStatus.BAD_REQUEST, "La respuesta debe incluir todas las ventanas de la solicitud.");
        }

        boolean cambioMedidas = false;
        for (SolicitudItem item : actuales) {
            ItemVentanaDto nuevo = porId.get(String.valueOf(item.getSolicitudItemId()));
            validarMedidas(nuevo.anchoMm(), nuevo.altoMm(), nuevo.cantidad());
            if (nuevo.precioUnitario() == null || nuevo.precioUnitario() < 1 || nuevo.precioUnitario() > MAX_PRECIO) {
                throw error(HttpStatus.BAD_REQUEST, "Ingresa el precio unitario de todas las ventanas.");
            }
            DatosVentana v = item.getVentana();
            cambioMedidas |= v.getAnchoMm() != nuevo.anchoMm() || v.getAltoMm() != nuevo.altoMm() || v.getCantidad() != nuevo.cantidad();
        }
        if (estado == Estado.ACEPTADA && cambioMedidas) {
            throw error(HttpStatus.BAD_REQUEST, "Cambiaste medidas o cantidades: usa Modificar para informarlo al cliente.");
        }
        if (estado == Estado.MODIFICADA && !cambioMedidas) {
            throw error(HttpStatus.BAD_REQUEST, "No cambiaste medidas ni cantidades. Si estás de acuerdo con lo pedido, usa Aceptar.");
        }

        for (SolicitudItem item : actuales) {
            ItemVentanaDto nuevo = porId.get(String.valueOf(item.getSolicitudItemId()));
            if (estado == Estado.MODIFICADA) {
                SolicitudItem copia = new SolicitudItem();
                copia.setSolicitud(solicitud);
                copia.setOriginal(true);
                copia.setVentana(item.getVentana().copia());
                solicitud.getItems().add(copia);
                item.getVentana().setAnchoMm(nuevo.anchoMm());
                item.getVentana().setAltoMm(nuevo.altoMm());
                item.getVentana().setCantidad(nuevo.cantidad());
            }
            item.setPrecioUnitario(nuevo.precioUnitario());
        }
    }

    // ---------- Avisos ----------

    @Transactional(readOnly = true)
    public List<AvisoDto> avisos(String destinatario) {
        return avisoRepository.findTop50ByDestinatarioOrderByFechaDesc(destinatario).stream()
                .map(a -> new AvisoDto(String.valueOf(a.getAvisoId()), a.getDestinatario(), iso(a.getFecha()),
                        a.getSolicitudNumero(), a.getTitulo(), a.getMensaje(), a.isLeido()))
                .toList();
    }

    @Transactional
    public void marcarAvisosLeidos(String destinatario) {
        avisoRepository.marcarLeidos(destinatario);
    }

    /** Clientes y administrador reciben sus propios avisos; los proveedores, los de solicitudes nuevas. */
    public static String destinatarioDe(Long cuentaId, Rol rol) {
        return rol == Rol.PROVEEDOR ? Aviso.PROVEEDORES : Aviso.deCuenta(cuentaId);
    }

    private void crearAviso(String destinatario, Long numero, String titulo, String mensaje) {
        Aviso aviso = new Aviso();
        aviso.setDestinatario(destinatario);
        aviso.setFecha(LocalDateTime.now());
        aviso.setSolicitudNumero(numero);
        aviso.setTitulo(titulo);
        aviso.setMensaje(mensaje);
        avisoRepository.save(aviso);
    }

    // ---------- Validación ----------

    private CarritoItem carritoDelCliente(Long cuentaId, Long itemId) {
        return carritoRepository.findByCarritoItemIdAndCuentaCuentaId(itemId, cuentaId)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "La ventana ya no está en el carrito."));
    }

    private DatosVentana validarVentana(ItemVentanaDto item) {
        if (item == null) throw error(HttpStatus.BAD_REQUEST, "Faltan los datos de la ventana.");
        validarMedidas(item.anchoMm(), item.altoMm(), item.cantidad());
        String descripcion = recortar(item.descripcion(), 255);
        String color = recortar(item.colorNombre(), 255);
        String vidrio = recortar(item.vidrioNombre(), 255);
        if (descripcion.isEmpty() || color.isEmpty() || vidrio.isEmpty()) {
            throw error(HttpStatus.BAD_REQUEST, "La ventana debe tener pauta, color y vidrio.");
        }
        DatosVentana v = new DatosVentana();
        v.setDescripcion(descripcion);
        v.setPautaId(item.pautaId());
        v.setSerieNombre(recortarONulo(item.serieNombre()));
        v.setImagenPauta(recortarONulo(item.imagenPauta()));
        v.setHojas(Math.max(1, Math.min(12, item.hojas())));
        v.setAnchoMm(item.anchoMm());
        v.setAltoMm(item.altoMm());
        v.setCantidad(item.cantidad());
        v.setColorId(item.colorId());
        v.setColorNombre(color);
        v.setVidrioId(item.vidrioId());
        v.setVidrioNombre(vidrio);
        v.setObservaciones(recortar(item.observaciones(), 1000));
        return v;
    }

    private static void validarMedidas(int anchoMm, int altoMm, int cantidad) {
        if (anchoMm < MIN_MM || anchoMm > MAX_ANCHO_MM) throw error(HttpStatus.BAD_REQUEST, "El ancho debe estar entre " + MIN_MM + " y " + MAX_ANCHO_MM + " mm.");
        if (altoMm < MIN_MM || altoMm > MAX_ALTO_MM) throw error(HttpStatus.BAD_REQUEST, "El alto debe estar entre " + MIN_MM + " y " + MAX_ALTO_MM + " mm.");
        if (cantidad < 1 || cantidad > MAX_CANTIDAD) throw error(HttpStatus.BAD_REQUEST, "La cantidad debe estar entre 1 y " + MAX_CANTIDAD + ".");
    }

    /** Igual que el front: el teléfono es opcional y la dirección solo se exige para instalación o flete. */
    private static ContactoDto validarContacto(ContactoDto contacto, Set<Servicio> servicios) {
        if (contacto == null) throw error(HttpStatus.BAD_REQUEST, "Faltan los datos de contacto.");
        String nombre = recortar(contacto.nombre(), 255);
        String email = recortar(contacto.email(), 255);
        String telefono = recortar(contacto.telefono(), 50);
        String direccion = recortar(contacto.direccion(), 255);
        if (nombre.isEmpty()) throw error(HttpStatus.BAD_REQUEST, "Ingresa tu nombre.");
        if (!EMAIL.matcher(email).matches()) throw error(HttpStatus.BAD_REQUEST, "Ingresa un correo válido.");
        if (direccion.isEmpty() && servicios.stream().anyMatch(s -> s != Servicio.FABRICACION)) {
            throw error(HttpStatus.BAD_REQUEST, "Indica la dirección para la instalación o el flete.");
        }
        return new ContactoDto(nombre, email, telefono, direccion);
    }

    private static Set<Servicio> validarServicios(List<String> servicios) {
        Set<Servicio> resultado = new LinkedHashSet<>();
        for (String s : servicios == null ? List.<String>of() : servicios) {
            try {
                resultado.add(Servicio.valueOf(s));
            } catch (IllegalArgumentException | NullPointerException e) {
                throw error(HttpStatus.BAD_REQUEST, "Servicio desconocido: " + s);
            }
        }
        if (resultado.isEmpty()) throw error(HttpStatus.BAD_REQUEST, "Elige al menos un servicio.");
        return resultado;
    }

    private static Estado parsearEstadoRespuesta(String estado) {
        try {
            Estado parseado = Estado.valueOf(estado);
            if (parseado != Estado.PENDIENTE) return parseado;
        } catch (IllegalArgumentException | NullPointerException ignored) {
            // cae al error de abajo
        }
        throw error(HttpStatus.BAD_REQUEST, "La respuesta debe ser ACEPTADA, MODIFICADA o RECHAZADA.");
    }

    // ---------- Conversión ----------

    private static List<SolicitudItem> vigentes(Solicitud solicitud) {
        return solicitud.getItems().stream().filter(i -> !i.isOriginal()).toList();
    }

    private SolicitudDto aDto(Solicitud s) {
        List<ItemVentanaDto> items = new ArrayList<>();
        List<ItemVentanaDto> originales = new ArrayList<>();
        for (SolicitudItem item : s.getItems()) {
            ItemVentanaDto dto = aDto(String.valueOf(item.getSolicitudItemId()), item.getVentana(), item.getPrecioUnitario());
            (item.isOriginal() ? originales : items).add(dto);
        }
        RespuestaDto respuesta = s.getEstado() == Estado.PENDIENTE ? null : new RespuestaDto(
                iso(s.getFechaRespuesta()), s.getMensajeRespuesta(), s.getTotal(),
                Boolean.TRUE.equals(s.getNotificadoEnApp()), Boolean.TRUE.equals(s.getNotificadoPorCorreo()));
        return new SolicitudDto(
                s.getNumero(), iso(s.getFecha()), s.getCliente().getEmail(),
                new ContactoDto(s.getContactoNombre(), s.getContactoEmail(), s.getContactoTelefono(), s.getContactoDireccion()),
                s.getServicios().stream().map(Enum::name).toList(), s.getObservaciones(),
                items, originales.isEmpty() ? null : originales, s.getEstado().name(), respuesta);
    }

    private static ItemVentanaDto aDto(String id, DatosVentana v, Long precio) {
        return new ItemVentanaDto(id, v.getDescripcion(), v.getPautaId(), v.getSerieNombre(), v.getImagenPauta(), v.getHojas(),
                v.getAnchoMm(), v.getAltoMm(), v.getCantidad(), v.getColorId(), v.getColorNombre(), v.getVidrioId(),
                v.getVidrioNombre(), v.getObservaciones() == null ? "" : v.getObservaciones(), precio);
    }

    /** Fecha con zona, para que el navegador la muestre en su hora local. */
    private static String iso(LocalDateTime fecha) {
        return fecha == null ? null : fecha.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString();
    }

    private static String recortar(String texto, int maximo) {
        if (texto == null) return "";
        String limpio = texto.trim();
        return limpio.length() > maximo ? limpio.substring(0, maximo) : limpio;
    }

    private static String recortarONulo(String texto) {
        String limpio = recortar(texto, 255);
        return limpio.isEmpty() ? null : limpio;
    }

    private static ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}
