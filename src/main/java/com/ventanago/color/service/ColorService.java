package com.ventanago.color.service;

import com.ventanago.color.service.dto.ColorDto;

import java.util.List;

public interface ColorService {

    List<ColorDto> obtenerColores();

    ColorDto obtenerColor(Long id);

    ColorDto agregarColor(ColorDto colorDto);

    ColorDto modificarColor(ColorDto colorDto);

    boolean eliminarColor(Long id);
}
