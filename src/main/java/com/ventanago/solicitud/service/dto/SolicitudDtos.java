package com.ventanago.solicitud.service.dto;

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

    public record RespuestaDto(String fecha, String mensaje, Long total, boolean notificadoEnApp, boolean notificadoPorCorreo) {
    }

    public record SolicitudDto(
            Long numero, String fecha, String usuario, ContactoDto contacto, List<String> servicios, String observaciones,
            List<ItemVentanaDto> items, List<ItemVentanaDto> itemsOriginales, String estado, RespuestaDto respuesta) {
    }

    public record NuevaSolicitudRequest(ContactoDto contacto, List<String> servicios, String observaciones) {
    }

    /** Solo se usan id, medidas, cantidad y precio de cada ventana. */
    public record RespuestaRequest(String estado, String mensaje, List<ItemVentanaDto> items,
                                   boolean notificarEnApp, boolean notificarPorCorreo) {
    }

    public record AvisoDto(String id, String destinatario, String fecha, Long solicitudNumero, String titulo,
                           String mensaje, boolean leido) {
    }
}
