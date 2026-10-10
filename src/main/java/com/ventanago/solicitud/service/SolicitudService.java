package com.ventanago.solicitud.service;

import com.ventanago.auth.repository.CuentaRepository;
import com.ventanago.auth.repository.entity.Cuenta;
import com.ventanago.auth.repository.entity.Rol;
import com.ventanago.comuna.repository.ComunaRepository;
import com.ventanago.comuna.repository.entity.Comuna;
import com.ventanago.proveedor.repository.entity.PerfilProveedor;
import com.ventanago.proveedor.service.ProveedorService;
import com.ventanago.proveedor.service.dto.ProveedorDtos.ProveedorResumenDto;
import com.ventanago.solicitud.repository.*;
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
 * Carrito del cliente, solicitudes de cotización, ofertas de los proveedores, chat y avisos.
 * Cada solicitud llega a los proveedores que cubren su comuna y servicios (o solo a los que eligió el cliente);
 * cada proveedor envía una oferta y el cliente elige una.
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
    private static final int MAX_PLAZO_DIAS = 365;
    private static final int MAX_INVITADOS = 10;
    private static final int MAX_FOTOS = 4;
    private static final int MAX_BYTES_FOTO = 1_500_000;
    private static final Set<String> TIPOS_FOTO = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final CarritoItemRepository carritoRepository;
    private final SolicitudRepository solicitudRepository;
    private final AvisoRepository avisoRepository;
    private final CuentaRepository cuentaRepository;
    private final OfertaRepository ofertaRepository;
    private final ValoracionRepository valoracionRepository;
    private final FotoSolicitudRepository fotoRepository;
    private final MensajeOfertaRepository mensajeRepository;
    private final ComunaRepository comunaRepository;
    private final ProveedorService proveedorService;

    /** Quién pide los datos: decide qué ofertas y qué datos de contacto ve. */
    private record Vista(Long cuentaId, Rol rol) {
        boolean proveedor() {
            return rol == Rol.PROVEEDOR;
        }
    }

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

    /** El cliente ve las suyas; cada proveedor, las que le llegaron o ya ofertó; el administrador, todas. */
    @Transactional(readOnly = true)
    public List<SolicitudDto> solicitudes(Long cuentaId, Rol rol) {
        Vista vista = new Vista(cuentaId, rol);
        List<Solicitud> solicitudes = switch (rol) {
            case CLIENTE -> solicitudRepository.findByClienteCuentaIdOrderByNumeroAsc(cuentaId);
            case ADMIN -> solicitudRepository.findAllByOrderByNumeroAsc();
            case PROVEEDOR -> {
                Map<Long, PerfilProveedor> perfiles = proveedorService.perfiles();
                yield solicitudRepository.findAllByOrderByNumeroAsc().stream()
                        .filter(s -> visibleParaProveedor(s, cuentaId, perfiles))
                        .toList();
            }
        };
        return aDtos(solicitudes, vista);
    }

    /**
     * Convierte el carrito del cliente en una solicitud, lo vacía y avisa a los proveedores que la reciben.
     * Sin ventanas en el carrito, la solicitud describe un trabajo (reparación, cambio de vidrio, visita técnica…).
     */
    @Transactional
    public SolicitudDto enviarSolicitud(Long cuentaId, NuevaSolicitudRequest datos) {
        List<CarritoItem> carrito = carritoRepository.findByCuentaCuentaIdOrderByCarritoItemIdAsc(cuentaId);
        Set<Servicio> servicios = validarServicios(datos.servicios());
        String observaciones = recortar(datos.observaciones(), 2000);
        if (carrito.isEmpty()) {
            if (servicios.contains(Servicio.FABRICACION)) {
                throw error(HttpStatus.BAD_REQUEST, "Para pedir fabricación, primero agrega tus ventanas al carrito.");
            }
            if (observaciones.length() < 10) {
                throw error(HttpStatus.BAD_REQUEST, "Describe el trabajo que necesitas (al menos 10 caracteres).");
            }
        }
        ContactoDto contacto = validarContacto(datos.contacto(), servicios);
        if (datos.comunaId() != null && !comunaRepository.existsById(datos.comunaId())) {
            throw error(HttpStatus.BAD_REQUEST, "La comuna elegida no existe.");
        }
        Set<Long> invitados = validarInvitados(datos.proveedores());
        List<FotoSolicitud> fotos = validarFotos(datos.fotos());

        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cuentaRepository.getReferenceById(cuentaId));
        solicitud.setFecha(LocalDateTime.now());
        solicitud.setContactoNombre(contacto.nombre());
        solicitud.setContactoEmail(contacto.email());
        solicitud.setContactoTelefono(contacto.telefono());
        solicitud.setContactoDireccion(contacto.direccion());
        solicitud.setServicios(servicios);
        solicitud.setObservaciones(observaciones);
        solicitud.setComunaId(datos.comunaId());
        solicitud.setInvitados(invitados);
        for (CarritoItem item : carrito) {
            SolicitudItem nuevo = new SolicitudItem();
            nuevo.setSolicitud(solicitud);
            nuevo.setVentana(item.getVentana().copia());
            solicitud.getItems().add(nuevo);
        }
        solicitudRepository.save(solicitud);
        for (FotoSolicitud foto : fotos) {
            foto.setSolicitud(solicitud);
            fotoRepository.save(foto);
        }
        carritoRepository.deleteAll(carrito);

        String detalle = carrito.isEmpty()
                ? contacto.nombre() + " pide " + servicios.stream().map(SolicitudService::nombreServicio).collect(Collectors.joining(", ")).toLowerCase(Locale.ROOT) + "."
                : contacto.nombre() + " solicitó cotizar " + carrito.size() + " tipo(s) de ventana.";
        Map<Long, PerfilProveedor> perfiles = proveedorService.perfiles();
        for (Cuenta proveedor : cuentaRepository.findByRolAndActivoTrue(Rol.PROVEEDOR)) {
            if (visibleParaProveedor(solicitud, proveedor.getCuentaId(), perfiles)) {
                crearAviso(Aviso.deCuenta(proveedor.getCuentaId()), solicitud.getNumero(),
                        (invitados.isEmpty() ? "Nueva solicitud N°" : "Te invitaron a cotizar la solicitud N°") + solicitud.getNumero(), detalle);
            }
        }
        return aDtos(List.of(solicitud), new Vista(cuentaId, Rol.CLIENTE)).get(0);
    }

    /**
     * El proveedor envía su oferta: acepta lo pedido con precio, lo modifica (medidas o cantidades) o no toma el trabajo.
     * Una oferta por proveedor y solicitud, mientras la solicitud siga abierta.
     */
    @Transactional
    public SolicitudDto responder(Long numero, Long proveedorId, Rol rol, RespuestaRequest respuesta) {
        Solicitud solicitud = buscar(numero);
        if (solicitud.getEstado() != Estado.PENDIENTE) {
            throw error(HttpStatus.CONFLICT, "La solicitud N°" + numero + " ya no recibe ofertas.");
        }
        if (rol == Rol.PROVEEDOR && !visibleParaProveedor(solicitud, proveedorId, proveedorService.perfiles())) {
            throw error(HttpStatus.FORBIDDEN, "Esta solicitud no está dirigida a ti.");
        }
        if (solicitud.getOfertas().stream().anyMatch(o -> o.getProveedor().getCuentaId().equals(proveedorId))) {
            throw error(HttpStatus.CONFLICT, "Ya enviaste tu oferta para la solicitud N°" + numero + ".");
        }
        Oferta.Tipo tipo = parsearTipoOferta(respuesta.estado());
        String mensaje = recortar(respuesta.mensaje(), 2000);
        if (tipo != Oferta.Tipo.ACEPTADA && mensaje.isEmpty()) {
            throw error(HttpStatus.BAD_REQUEST, tipo == Oferta.Tipo.RECHAZADA ? "Indica el motivo por el que no tomas el trabajo." : "Explica al cliente qué modificaste.");
        }
        if (respuesta.plazoDias() != null && (respuesta.plazoDias() < 1 || respuesta.plazoDias() > MAX_PLAZO_DIAS)) {
            throw error(HttpStatus.BAD_REQUEST, "El plazo debe estar entre 1 y " + MAX_PLAZO_DIAS + " días.");
        }

        Oferta oferta = new Oferta();
        oferta.setSolicitud(solicitud);
        oferta.setProveedor(cuentaRepository.getReferenceById(proveedorId));
        oferta.setFecha(LocalDateTime.now());
        oferta.setTipo(tipo);
        oferta.setMensaje(mensaje);
        oferta.setNotificadoEnApp(respuesta.notificarEnApp());
        if (tipo != Oferta.Tipo.RECHAZADA) {
            oferta.setPlazoDias(respuesta.plazoDias());
            if (vigentes(solicitud).isEmpty()) {
                if (tipo == Oferta.Tipo.MODIFICADA) throw error(HttpStatus.BAD_REQUEST, "Esta solicitud no tiene ventanas que modificar: usa Aceptar.");
                if (respuesta.total() == null || respuesta.total() < 1 || respuesta.total() > MAX_PRECIO) {
                    throw error(HttpStatus.BAD_REQUEST, "Ingresa el precio total del trabajo.");
                }
                oferta.setTotal(respuesta.total());
            } else {
                agregarItems(solicitud, oferta, respuesta.items());
                oferta.setTotal(oferta.getItems().stream().mapToLong(i -> i.getPrecioUnitario() * i.getVentana().getCantidad()).sum());
            }
        }
        ofertaRepository.save(oferta);
        solicitud.getOfertas().add(oferta);

        if (tipo != Oferta.Tipo.RECHAZADA && respuesta.notificarEnApp()) {
            String nombre = proveedorService.resumenes(List.of(proveedorId)).get(proveedorId).nombre();
            crearAviso(Aviso.deCuenta(solicitud.getCliente().getCuentaId()), numero,
                    "Nueva oferta para tu solicitud N°" + numero,
                    nombre + (tipo == Oferta.Tipo.MODIFICADA ? " propone cambios por " : " ofrece hacerlo por ") + pesos(oferta.getTotal()) + ".");
        }
        return aDtos(List.of(solicitud), new Vista(proveedorId, rol)).get(0);
    }

    /**
     * Aceptar: solo se definen precios. Modificar: además cambian medidas o cantidades.
     * Las ventanas de la oferta son copias de las de la solicitud, que no cambia.
     */
    private void agregarItems(Solicitud solicitud, Oferta oferta, List<ItemVentanaDto> recibidos) {
        List<SolicitudItem> actuales = vigentes(solicitud);
        Map<String, ItemVentanaDto> porId = (recibidos == null ? List.<ItemVentanaDto>of() : recibidos).stream()
                .filter(i -> i.id() != null)
                .collect(Collectors.toMap(ItemVentanaDto::id, Function.identity(), (a, b) -> a));
        if (porId.size() != actuales.size() || !actuales.stream().allMatch(i -> porId.containsKey(String.valueOf(i.getSolicitudItemId())))) {
            throw error(HttpStatus.BAD_REQUEST, "La oferta debe incluir todas las ventanas de la solicitud.");
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
        if (oferta.getTipo() == Oferta.Tipo.ACEPTADA && cambioMedidas) {
            throw error(HttpStatus.BAD_REQUEST, "Cambiaste medidas o cantidades: usa Modificar para informarlo al cliente.");
        }
        if (oferta.getTipo() == Oferta.Tipo.MODIFICADA && !cambioMedidas) {
            throw error(HttpStatus.BAD_REQUEST, "No cambiaste medidas ni cantidades. Si estás de acuerdo con lo pedido, usa Aceptar.");
        }

        for (SolicitudItem item : actuales) {
            ItemVentanaDto nuevo = porId.get(String.valueOf(item.getSolicitudItemId()));
            OfertaItem copia = new OfertaItem();
            copia.setOferta(oferta);
            copia.setSolicitudItemId(item.getSolicitudItemId());
            copia.setVentana(item.getVentana().copia());
            copia.getVentana().setAnchoMm(nuevo.anchoMm());
            copia.getVentana().setAltoMm(nuevo.altoMm());
            copia.getVentana().setCantidad(nuevo.cantidad());
            copia.setPrecioUnitario(nuevo.precioUnitario());
            oferta.getItems().add(copia);
        }
    }

    /** El cliente elige una oferta: la solicitud queda adjudicada, las demás ofertas descartadas y todos avisados. */
    @Transactional
    public SolicitudDto elegirOferta(Long numero, Long ofertaId, Long cuentaId, Rol rol) {
        Solicitud solicitud = delCliente(numero, cuentaId, rol);
        if (solicitud.getEstado() != Estado.PENDIENTE) {
            throw error(HttpStatus.CONFLICT, "La solicitud N°" + numero + " ya tiene una oferta elegida o fue cancelada.");
        }
        Oferta elegida = solicitud.getOfertas().stream().filter(o -> o.getOfertaId().equals(ofertaId)).findFirst()
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "La oferta no es de esta solicitud."));
        if (elegida.getTipo() == Oferta.Tipo.RECHAZADA) throw error(HttpStatus.BAD_REQUEST, "Ese proveedor no tomó el trabajo.");

        for (Oferta o : solicitud.getOfertas()) {
            if (o == elegida) continue;
            o.setEstado(Oferta.Estado.DESCARTADA);
            if (o.getTipo() != Oferta.Tipo.RECHAZADA) {
                crearAviso(Aviso.deCuenta(o.getProveedor().getCuentaId()), numero, "Solicitud N°" + numero + " adjudicada a otro proveedor",
                        "El cliente eligió otra oferta. ¡Gracias por cotizar!");
            }
        }
        elegida.setEstado(Oferta.Estado.ELEGIDA);
        solicitud.setEstado(Estado.ADJUDICADA);
        solicitud.setRespondidaPor(elegida.getProveedor());
        solicitud.setFechaRespuesta(elegida.getFecha());
        solicitud.setMensajeRespuesta(elegida.getMensaje());
        solicitud.setTotal(elegida.getTotal());
        crearAviso(Aviso.deCuenta(elegida.getProveedor().getCuentaId()), numero, "¡Te eligieron para la solicitud N°" + numero + "!",
                solicitud.getContactoNombre() + " aceptó tu oferta de " + pesos(elegida.getTotal()) + ". Ya puedes ver sus datos de contacto.");
        return aDtos(List.of(solicitud), new Vista(cuentaId, rol)).get(0);
    }

    /** El cliente retira su solicitud (abierta o adjudicada, mientras no se haya terminado). */
    @Transactional
    public SolicitudDto cancelar(Long numero, Long cuentaId, Rol rol) {
        Solicitud solicitud = delCliente(numero, cuentaId, rol);
        if (solicitud.getEstado() != Estado.PENDIENTE && solicitud.getEstado() != Estado.ADJUDICADA) {
            throw error(HttpStatus.CONFLICT, "La solicitud N°" + numero + " ya no se puede cancelar.");
        }
        for (Oferta o : solicitud.getOfertas()) {
            if (o.getEstado() != Oferta.Estado.DESCARTADA && o.getTipo() != Oferta.Tipo.RECHAZADA) {
                crearAviso(Aviso.deCuenta(o.getProveedor().getCuentaId()), numero, "Solicitud N°" + numero + " cancelada",
                        "El cliente canceló su solicitud.");
            }
            o.setEstado(Oferta.Estado.DESCARTADA);
        }
        solicitud.setEstado(Estado.CANCELADA);
        return aDtos(List.of(solicitud), new Vista(cuentaId, rol)).get(0);
    }

    /** Con el trabajo hecho, el cliente valora al proveedor elegido y la solicitud queda terminada. */
    @Transactional
    public SolicitudDto valorar(Long numero, Long cuentaId, Rol rol, ValoracionRequest datos) {
        Solicitud solicitud = delCliente(numero, cuentaId, rol);
        if (solicitud.getEstado() != Estado.ADJUDICADA) {
            throw error(HttpStatus.CONFLICT, "Solo puedes valorar un trabajo adjudicado que aún no hayas valorado.");
        }
        if (datos.estrellas() < 1 || datos.estrellas() > 5) throw error(HttpStatus.BAD_REQUEST, "Elige de 1 a 5 estrellas.");
        Oferta elegida = solicitud.getOfertas().stream().filter(o -> o.getEstado() == Oferta.Estado.ELEGIDA).findFirst()
                .orElseThrow(() -> error(HttpStatus.CONFLICT, "La solicitud no tiene una oferta elegida."));
        Valoracion valoracion = new Valoracion();
        valoracion.setSolicitud(solicitud);
        valoracion.setProveedor(elegida.getProveedor());
        valoracion.setEstrellas(datos.estrellas());
        valoracion.setComentario(recortar(datos.comentario(), 1000));
        valoracion.setFecha(LocalDateTime.now());
        valoracionRepository.save(valoracion);
        solicitud.setEstado(Estado.TERMINADA);
        crearAviso(Aviso.deCuenta(elegida.getProveedor().getCuentaId()), numero, "Te valoraron con " + datos.estrellas() + " estrella(s)",
                valoracion.getComentario().isEmpty() ? "Solicitud N°" + numero + " terminada." : valoracion.getComentario());
        return aDtos(List.of(solicitud), new Vista(cuentaId, rol)).get(0);
    }

    // ---------- Fotos ----------

    @Transactional(readOnly = true)
    public FotoSolicitud foto(Long numero, Long fotoId, Long cuentaId, Rol rol) {
        Solicitud solicitud = buscar(numero);
        if (!puedeVer(solicitud, cuentaId, rol)) throw error(HttpStatus.FORBIDDEN, "No tienes acceso a esta solicitud.");
        FotoSolicitud foto = fotoRepository.findByFotoIdAndSolicitudNumero(fotoId, numero)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe la foto."));
        foto.getDatos(); // se carga dentro de la transacción
        return foto;
    }

    // ---------- Chat de cada oferta ----------

    @Transactional(readOnly = true)
    public List<MensajeDto> mensajes(Long ofertaId, Long cuentaId, Rol rol) {
        Oferta oferta = ofertaParticipante(ofertaId, cuentaId, rol);
        return mensajeRepository.findByOfertaOfertaIdOrderByFechaAscMensajeIdAsc(oferta.getOfertaId()).stream()
                .map(m -> aDto(m, cuentaId))
                .toList();
    }

    @Transactional
    public MensajeDto enviarMensaje(Long ofertaId, Long cuentaId, Rol rol, String texto) {
        Oferta oferta = ofertaParticipante(ofertaId, cuentaId, rol);
        if (oferta.getEstado() == Oferta.Estado.DESCARTADA || oferta.getSolicitud().getEstado() == Estado.CANCELADA) {
            throw error(HttpStatus.CONFLICT, "Esta conversación está cerrada.");
        }
        String limpio = recortar(texto, 1000);
        if (limpio.isEmpty()) throw error(HttpStatus.BAD_REQUEST, "Escribe un mensaje.");
        MensajeOferta mensaje = new MensajeOferta();
        mensaje.setOferta(oferta);
        mensaje.setAutor(cuentaRepository.getReferenceById(cuentaId));
        mensaje.setFecha(LocalDateTime.now());
        mensaje.setTexto(limpio);
        mensajeRepository.save(mensaje);

        Long numero = oferta.getSolicitud().getNumero();
        Long clienteId = oferta.getSolicitud().getCliente().getCuentaId();
        Long proveedorId = oferta.getProveedor().getCuentaId();
        String vistaPrevia = limpio.length() > 120 ? limpio.substring(0, 120) + "…" : limpio;
        if (!cuentaId.equals(clienteId)) {
            String nombre = proveedorService.resumenes(List.of(proveedorId)).get(proveedorId).nombre();
            crearAviso(Aviso.deCuenta(clienteId), numero, "Mensaje de " + nombre + " (solicitud N°" + numero + ")", vistaPrevia);
        }
        if (!cuentaId.equals(proveedorId)) {
            crearAviso(Aviso.deCuenta(proveedorId), numero, "Mensaje del cliente (solicitud N°" + numero + ")", vistaPrevia);
        }
        return aDto(mensaje, cuentaId);
    }

    private Oferta ofertaParticipante(Long ofertaId, Long cuentaId, Rol rol) {
        Oferta oferta = ofertaRepository.findById(ofertaId).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe la oferta."));
        boolean participa = rol == Rol.ADMIN
                || oferta.getProveedor().getCuentaId().equals(cuentaId)
                || oferta.getSolicitud().getCliente().getCuentaId().equals(cuentaId);
        if (!participa) throw error(HttpStatus.FORBIDDEN, "No participas en esta conversación.");
        return oferta;
    }

    private MensajeDto aDto(MensajeOferta m, Long cuentaId) {
        Long autorId = m.getAutor().getCuentaId();
        Oferta oferta = m.getOferta();
        String autor = autorId.equals(oferta.getSolicitud().getCliente().getCuentaId()) ? "Cliente"
                : autorId.equals(oferta.getProveedor().getCuentaId()) ? "Proveedor" : "VentanaGo";
        return new MensajeDto(m.getMensajeId(), iso(m.getFecha()), autor, autorId.equals(cuentaId), m.getTexto());
    }

    // ---------- Precio referencial ----------

    /** Cuartiles del precio por m² de las ventanas ofrecidas. Con menos de 3 muestras no se informa rango. */
    @Transactional(readOnly = true)
    public PrecioReferenciaDto precioReferencia() {
        List<Double> porM2 = ofertaRepository.preciosOfrecidos().stream()
                .map(f -> ((Number) f[0]).doubleValue() / (((Number) f[1]).doubleValue() * ((Number) f[2]).doubleValue() / 1_000_000d))
                .sorted()
                .toList();
        if (porM2.size() < 3) return new PrecioReferenciaDto(porM2.size(), null, null, null);
        return new PrecioReferenciaDto(porM2.size(), percentil(porM2, 0.25), percentil(porM2, 0.5), percentil(porM2, 0.75));
    }

    private static Long percentil(List<Double> ordenados, double p) {
        double posicion = p * (ordenados.size() - 1);
        int abajo = (int) Math.floor(posicion);
        int arriba = (int) Math.ceil(posicion);
        double valor = ordenados.get(abajo) + (ordenados.get(arriba) - ordenados.get(abajo)) * (posicion - abajo);
        return Math.round(valor);
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

    /** Cada cuenta recibe sus propios avisos (los proveedores, solo los de las solicitudes que les llegan). */
    public static String destinatarioDe(Long cuentaId, Rol rol) {
        return Aviso.deCuenta(cuentaId);
    }

    private void crearAviso(String destinatario, Long numero, String titulo, String mensaje) {
        Aviso aviso = new Aviso();
        aviso.setDestinatario(destinatario);
        aviso.setFecha(LocalDateTime.now());
        aviso.setSolicitudNumero(numero);
        aviso.setTitulo(titulo.length() > 255 ? titulo.substring(0, 255) : titulo);
        aviso.setMensaje(mensaje);
        avisoRepository.save(aviso);
    }

    // ---------- Quién ve qué ----------

    /**
     * El proveedor ve las solicitudes que ya ofertó y las abiertas que le llegan: si el cliente eligió proveedores,
     * solo esos; si no, los que cubren la comuna y alguno de los servicios.
     */
    private static boolean visibleParaProveedor(Solicitud s, Long proveedorId, Map<Long, PerfilProveedor> perfiles) {
        if (s.getOfertas().stream().anyMatch(o -> o.getProveedor().getCuentaId().equals(proveedorId))) return true;
        if (s.getEstado() != Estado.PENDIENTE) return false;
        if (!s.getInvitados().isEmpty()) return s.getInvitados().contains(proveedorId);
        PerfilProveedor perfil = perfiles.get(proveedorId);
        return perfil == null || ProveedorService.atiende(perfil, s.getComunaId(), s.getServicios());
    }

    private boolean puedeVer(Solicitud s, Long cuentaId, Rol rol) {
        return switch (rol) {
            case ADMIN -> true;
            case CLIENTE -> s.getCliente().getCuentaId().equals(cuentaId);
            case PROVEEDOR -> visibleParaProveedor(s, cuentaId, proveedorService.perfiles());
        };
    }

    private Solicitud buscar(Long numero) {
        return solicitudRepository.findById(numero)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No existe la solicitud N°" + numero + "."));
    }

    /** La solicitud, si es del cliente que la pide (el administrador puede actuar sobre cualquiera). */
    private Solicitud delCliente(Long numero, Long cuentaId, Rol rol) {
        Solicitud solicitud = buscar(numero);
        if (rol != Rol.ADMIN && !solicitud.getCliente().getCuentaId().equals(cuentaId)) {
            throw error(HttpStatus.FORBIDDEN, "La solicitud N°" + numero + " no es tuya.");
        }
        return solicitud;
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

    /** Igual que el front: el teléfono es opcional y la dirección solo se exige si alguien debe ir al lugar. */
    private static ContactoDto validarContacto(ContactoDto contacto, Set<Servicio> servicios) {
        if (contacto == null) throw error(HttpStatus.BAD_REQUEST, "Faltan los datos de contacto.");
        String nombre = recortar(contacto.nombre(), 255);
        String email = recortar(contacto.email(), 255);
        String telefono = recortar(contacto.telefono(), 50);
        String direccion = recortar(contacto.direccion(), 255);
        if (nombre.isEmpty()) throw error(HttpStatus.BAD_REQUEST, "Ingresa tu nombre.");
        if (!EMAIL.matcher(email).matches()) throw error(HttpStatus.BAD_REQUEST, "Ingresa un correo válido.");
        if (direccion.isEmpty() && servicios.stream().anyMatch(s -> s != Servicio.FABRICACION)) {
            throw error(HttpStatus.BAD_REQUEST, "Indica la dirección donde se hará el trabajo.");
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

    private Set<Long> validarInvitados(List<Long> proveedores) {
        Set<Long> ids = new LinkedHashSet<>(proveedores == null ? List.of() : proveedores);
        if (ids.size() > MAX_INVITADOS) throw error(HttpStatus.BAD_REQUEST, "Puedes elegir hasta " + MAX_INVITADOS + " proveedores.");
        if (ids.isEmpty()) return ids;
        long validos = cuentaRepository.findAllById(ids).stream().filter(c -> c.getRol() == Rol.PROVEEDOR && c.isActivo()).count();
        if (validos != ids.size()) throw error(HttpStatus.BAD_REQUEST, "Alguno de los proveedores elegidos ya no está disponible.");
        return ids;
    }

    private static List<FotoSolicitud> validarFotos(List<FotoNuevaDto> fotos) {
        List<FotoSolicitud> resultado = new ArrayList<>();
        if (fotos == null) return resultado;
        if (fotos.size() > MAX_FOTOS) throw error(HttpStatus.BAD_REQUEST, "Puedes adjuntar hasta " + MAX_FOTOS + " fotos.");
        for (FotoNuevaDto f : fotos) {
            if (f == null || !TIPOS_FOTO.contains(f.tipoContenido())) throw error(HttpStatus.BAD_REQUEST, "Las fotos deben ser JPEG, PNG o WebP.");
            byte[] datos;
            try {
                datos = Base64.getDecoder().decode(f.datos() == null ? "" : f.datos());
            } catch (IllegalArgumentException e) {
                throw error(HttpStatus.BAD_REQUEST, "Una de las fotos llegó dañada.");
            }
            if (datos.length == 0 || datos.length > MAX_BYTES_FOTO) throw error(HttpStatus.BAD_REQUEST, "Cada foto debe pesar menos de 1,5 MB.");
            FotoSolicitud foto = new FotoSolicitud();
            foto.setTipoContenido(f.tipoContenido());
            foto.setDatos(datos);
            resultado.add(foto);
        }
        return resultado;
    }

    private static Oferta.Tipo parsearTipoOferta(String tipo) {
        try {
            return Oferta.Tipo.valueOf(tipo);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw error(HttpStatus.BAD_REQUEST, "La respuesta debe ser ACEPTADA, MODIFICADA o RECHAZADA.");
        }
    }

    // ---------- Conversión ----------

    private static List<SolicitudItem> vigentes(Solicitud solicitud) {
        return solicitud.getItems().stream().filter(i -> !i.isOriginal()).toList();
    }

    /** Convierte varias solicitudes juntando las consultas de proveedores, comunas y mensajes. */
    private List<SolicitudDto> aDtos(List<Solicitud> solicitudes, Vista vista) {
        Set<Long> proveedores = new HashSet<>();
        Set<Long> ofertas = new HashSet<>();
        Set<Long> comunas = new HashSet<>();
        for (Solicitud s : solicitudes) {
            for (Oferta o : s.getOfertas()) {
                proveedores.add(o.getProveedor().getCuentaId());
                ofertas.add(o.getOfertaId());
            }
            if (s.getComunaId() != null) comunas.add(s.getComunaId());
        }
        Map<Long, ProveedorResumenDto> resumenes = proveedorService.resumenes(proveedores);
        Map<Long, Long> mensajes = ofertas.isEmpty() ? Map.of() : mensajeRepository.contarPorOferta(ofertas).stream()
                .collect(Collectors.toMap(f -> (Long) f[0], f -> (Long) f[1]));
        Map<Long, ComunaRefDto> refComunas = comunaRepository.findAllById(comunas).stream()
                .collect(Collectors.toMap(Comuna::getComunaId, c -> new ComunaRefDto(c.getComunaId(), c.getNombre(), c.getRegion().getNombre())));
        return solicitudes.stream().map(s -> aDto(s, vista, resumenes, mensajes, refComunas)).toList();
    }

    private SolicitudDto aDto(Solicitud s, Vista vista, Map<Long, ProveedorResumenDto> resumenes, Map<Long, Long> mensajes,
                              Map<Long, ComunaRefDto> comunas) {
        List<ItemVentanaDto> items = vigentes(s).stream()
                .map(i -> aDto(String.valueOf(i.getSolicitudItemId()), i.getVentana(), null))
                .toList();
        List<OfertaDto> ofertas = s.getOfertas().stream().map(o -> aDto(o, resumenes, mensajes)).toList();
        OfertaDto miOferta = vista.proveedor()
                ? ofertas.stream().filter(o -> o.proveedor().cuentaId().equals(vista.cuentaId())).findFirst().orElse(null)
                : null;
        OfertaDto elegida = ofertas.stream().filter(o -> Oferta.Estado.ELEGIDA.name().equals(o.estado())).findFirst().orElse(null);

        // Antes de ser elegido, el proveedor solo ve el primer nombre del cliente y la comuna.
        boolean contactoCompleto = !vista.proveedor() || (miOferta != null && miOferta == elegida);
        ContactoDto contacto = contactoCompleto
                ? new ContactoDto(s.getContactoNombre(), s.getContactoEmail(), s.getContactoTelefono(), s.getContactoDireccion())
                : new ContactoDto(primerNombre(s.getContactoNombre()), "", "", "");

        OfertaDto paraRespuesta = vista.proveedor() ? miOferta : elegida;
        RespuestaDto respuesta = paraRespuesta == null ? null
                : new RespuestaDto(paraRespuesta.fecha(), paraRespuesta.mensaje(), paraRespuesta.total(), true, false);
        ValoracionDto valoracion = s.getEstado() == Estado.TERMINADA
                ? valoracionRepository.findBySolicitudNumero(s.getNumero()).map(ProveedorService::aDto).orElse(null)
                : null;
        int cantidadOfertas = (int) s.getOfertas().stream().filter(o -> o.getTipo() != Oferta.Tipo.RECHAZADA).count();

        return new SolicitudDto(
                s.getNumero(), iso(s.getFecha()), contactoCompleto ? s.getCliente().getEmail() : null, contacto,
                s.getServicios().stream().map(Enum::name).toList(), s.getObservaciones(),
                items, null, s.getEstado().name(), respuesta,
                s.getComunaId() == null ? null : comunas.get(s.getComunaId()),
                vista.proveedor() ? List.of() : List.copyOf(s.getInvitados()),
                vista.proveedor() ? null : ofertas, miOferta, cantidadOfertas,
                fotoRepository.idsDeSolicitud(s.getNumero()), valoracion);
    }

    private OfertaDto aDto(Oferta o, Map<Long, ProveedorResumenDto> resumenes, Map<Long, Long> mensajes) {
        Long proveedorId = o.getProveedor().getCuentaId();
        List<ItemVentanaDto> items = o.getItems().stream()
                .map(i -> aDto(String.valueOf(i.getSolicitudItemId()), i.getVentana(), i.getPrecioUnitario()))
                .toList();
        return new OfertaDto(o.getOfertaId(), o.getSolicitud().getNumero(), resumenes.get(proveedorId), iso(o.getFecha()),
                o.getTipo().name(), o.getEstado().name(), o.getMensaje(), o.getTotal(), o.getPlazoDias(), items,
                o.getEstado() == Oferta.Estado.ELEGIDA ? proveedorService.contacto(proveedorId) : null,
                mensajes.getOrDefault(o.getOfertaId(), 0L));
    }

    private static ItemVentanaDto aDto(String id, DatosVentana v, Long precio) {
        return new ItemVentanaDto(id, v.getDescripcion(), v.getPautaId(), v.getSerieNombre(), v.getImagenPauta(), v.getHojas(),
                v.getAnchoMm(), v.getAltoMm(), v.getCantidad(), v.getColorId(), v.getColorNombre(), v.getVidrioId(),
                v.getVidrioNombre(), v.getObservaciones() == null ? "" : v.getObservaciones(), precio);
    }

    private static String primerNombre(String nombre) {
        return nombre == null || nombre.isBlank() ? "Cliente" : nombre.trim().split("\\s+")[0];
    }

    public static String nombreServicio(Servicio s) {
        return switch (s) {
            case FABRICACION -> "Fabricación";
            case INSTALACION -> "Instalación";
            case FLETE -> "Flete";
            case REPARACION -> "Reparación";
            case CAMBIO_VIDRIO -> "Cambio de vidrio";
            case MANTENCION -> "Mantención";
            case MEDICION -> "Visita técnica";
        };
    }

    private static String pesos(Long valor) {
        return valor == null ? "" : "$" + String.format(Locale.forLanguageTag("es-CL"), "%,d", valor).replace(',', '.');
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
