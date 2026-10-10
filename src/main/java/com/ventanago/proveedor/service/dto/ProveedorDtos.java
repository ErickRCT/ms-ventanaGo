package com.ventanago.proveedor.service.dto;

import com.ventanago.solicitud.service.dto.SolicitudDtos.ValoracionDto;

import java.util.List;

public final class ProveedorDtos {

    private ProveedorDtos() {
    }

    public record ComunaNombreDto(Long comunaId, String nombre) {
    }

    /** Lo que el cliente ve de un proveedor al comparar ofertas o buscar en su zona. */
    public record ProveedorResumenDto(
            Long cuentaId, String nombre, String descripcion, boolean verificado, int aniosExperiencia,
            Double promedio, long valoraciones, long trabajos, List<String> servicios, List<ComunaNombreDto> comunas) {
    }

    public record ProveedorDetalleDto(ProveedorResumenDto proveedor, List<ValoracionDto> valoraciones) {
    }

    /** Perfil que edita el propio proveedor. */
    public record PerfilRequest(String nombreComercial, String descripcion, String telefono, int aniosExperiencia,
                                List<String> servicios, List<Long> comunas) {
    }

    public record PerfilDto(String nombreComercial, String descripcion, String telefono, int aniosExperiencia,
                            boolean verificado, List<String> servicios, List<ComunaNombreDto> comunas) {
    }

    public record VerificadoRequest(boolean verificado) {
    }
}
