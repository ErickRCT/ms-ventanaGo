package com.ventanago.solicitud.service.dto;

import com.ventanago.proveedor.service.dto.ProveedorDtos.ProveedorResumenDto;

import java.util.List;

/** Mismas formas que los tipos del front (src/modules/solicitudes/tipos.ts). Los ids viajan como texto. */
public final class SolicitudDtos {

    private SolicitudDtos() {
    }

    public record ItemVentanaDto(
            String id, String descripcion, Long pautaId, String serieNombre, String imagenPauta, int hojas,
            int anchoMm, int altoMm, int cantidad, Long colorId, String colorNombre, Long vidrioId, String vidrioNombre,
            String observaciones, Long precioUnitario) {
    }

    public record CantidadRequest(int cantidad) {
    }

    public record ContactoDto(String nombre, String email, String telefono, String direccion) {
    }

    /** Respuesta en la forma anterior (una por solicitud): la oferta elegida, o la propia para el proveedor. */
    public record RespuestaDto(String fecha, String mensaje, Long total, boolean notificadoEnApp, boolean notificadoPorCorreo) {
    }

    public record ComunaRefDto(Long comunaId, String nombre, String region) {
    }

    /**
     * [contactoProveedor] solo llega cuando la oferta fue elegida: antes, cliente y proveedor conversan por el chat.
     * [items] trae las ventanas con precio (y medidas, si el proveedor las cambió).
     */
    public record OfertaDto(
            Long id, Long numero, ProveedorResumenDto proveedor, String fecha, String tipo, String estado, String mensaje,
            Long total, Integer plazoDias, List<ItemVentanaDto> items, ContactoDto contactoProveedor, long mensajes) {
    }

    public record ValoracionDto(int estrellas, String comentario, String fecha, String autor) {
    }

    /**
     * El cliente recibe todas las ofertas; cada proveedor, solo la suya en [miOferta]. El proveedor ve el contacto
     * completo del cliente recién cuando su oferta es elegida.
     */
    public record SolicitudDto(
            Long numero, String fecha, String usuario, ContactoDto contacto, List<String> servicios, String observaciones,
            List<ItemVentanaDto> items, List<ItemVentanaDto> itemsOriginales, String estado, RespuestaDto respuesta,
            ComunaRefDto comuna, List<Long> invitados, List<OfertaDto> ofertas, OfertaDto miOferta, int cantidadOfertas,
            List<Long> fotos, ValoracionDto valoracion) {
    }

    /** [datos]: imagen en base64 (JPEG, PNG o WebP). */
    public record FotoNuevaDto(String tipoContenido, String datos) {
    }

    /**
     * [comunaId] y [proveedores] son opcionales para no romper el front web anterior: sin comuna la solicitud llega
     * a todos los proveedores; sin proveedores, a todos los de la comuna.
     */
    public record NuevaSolicitudRequest(ContactoDto contacto, List<String> servicios, String observaciones,
                                        Long comunaId, List<Long> proveedores, List<FotoNuevaDto> fotos) {
    }

    /**
     * Oferta del proveedor. Solo se usan id, medidas, cantidad y precio de cada ventana.
     * [total] solo se usa en solicitudes sin ventanas (reparaciones, visitas técnicas).
     */
    public record RespuestaRequest(String estado, String mensaje, List<ItemVentanaDto> items,
                                   boolean notificarEnApp, boolean notificarPorCorreo, Long total, Integer plazoDias) {
    }

    public record ValoracionRequest(int estrellas, String comentario) {
    }

    public record MensajeDto(Long id, String fecha, String autor, boolean mio, String texto) {
    }

    public record NuevoMensajeRequest(String texto) {
    }

    /** Precio por m² de las ventanas ofrecidas, para mostrar un rango antes de pedir cotización. */
    public record PrecioReferenciaDto(int muestras, Long p25PorM2, Long medianaPorM2, Long p75PorM2) {
    }

    public record AvisoDto(String id, String destinatario, String fecha, Long solicitudNumero, String titulo,
                           String mensaje, boolean leido) {
    }
}
