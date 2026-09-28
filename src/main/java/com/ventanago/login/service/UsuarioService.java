package com.ventanago.login.service;

import com.ventanago.login.service.dto.UsuarioDto;

import java.util.List;

public interface UsuarioService {
    List<UsuarioDto> findAll();

    UsuarioDto findByUsuario(String usuario);

    UsuarioDto agregarUsuario(UsuarioDto usuarioDto);
}
