package com.ventanago.cotizacion.service;

import com.ventanago.cotizacion.service.dto.CotizacionDto;

import java.util.List;

public interface CotizacionService {
    List<CotizacionDto> obtenerCotizaciones();

    CotizacionDto obtenerCotizacion(Long id);

    CotizacionDto agregarCotizacion(CotizacionDto cotizacionDto);

    CotizacionDto modificarCotizacion(CotizacionDto cotizacionDto);

    /** Recalcula neto, m² y unidades a partir de las ventanas guardadas (reemplaza los triggers de la BD antigua). */
    void recalcularTotales(Long cotizacionId);
}
