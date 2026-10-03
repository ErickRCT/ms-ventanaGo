package com.ventanago.ventana.service;

import com.ventanago.ventana.service.dto.VentanaDto;

import java.util.List;

public interface VentanaService {

    List<VentanaDto> obtenerVentanas();

    VentanaDto obtenerVentana(Long id);

    VentanaDto cotizarYGuardarVentana(VentanaDto ventanaDto);

    VentanaDto cotizarVentana(VentanaDto ventanaDto);

    boolean eliminarVentana(Long id);
}
