package com.ventanago.perfileria.service;

import com.ventanago.perfileria.service.dto.TipoPerfilDto;

import java.util.List;

public interface TipoPerfilService {
    List<TipoPerfilDto> getTipoPerfiles();

    TipoPerfilDto buscarTipoPerfil(Long id);

    TipoPerfilDto agregarTipoPerfil(TipoPerfilDto tipoPerfilDto);

    TipoPerfilDto modificarTipoPerfil(TipoPerfilDto tipoPerfilDto);

    boolean eliminarTipoPerfil(Long id);

}
