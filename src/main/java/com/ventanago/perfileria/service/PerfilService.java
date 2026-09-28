package com.ventanago.perfileria.service;

import com.ventanago.perfileria.service.dto.PerfilDto;

import java.util.List;

public interface PerfilService {


    List<PerfilDto> obtenerPerfiles();

    PerfilDto buscarPerfil(Long id);

    PerfilDto agregarPerfil(PerfilDto perfilDto);

    PerfilDto modificarPerfil(PerfilDto perfilDto);

    boolean eliminarPerfil(Long id);

    List<PerfilDto> obtenerPerfilesPorSerieId(Long serieId);

    List<PerfilDto> obtenerPerfilesPorTipoPerfilId(Long tipoPerfilId);
}
