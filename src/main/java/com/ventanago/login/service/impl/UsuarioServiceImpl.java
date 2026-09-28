package com.ventanago.login.service.impl;

import com.ventanago.login.repository.UsuarioRepository;
import com.ventanago.login.service.UsuarioService;
import com.ventanago.login.service.dto.UsuarioDto;
import com.ventanago.login.service.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    UsuarioRepository usuarioRepository;
    UsuarioMapper usuarioMapper;

    @Override
    public List<UsuarioDto> findAll() {
        return usuarioMapper.toUsuarioDtoList(usuarioRepository.findAll());
    }

    @Override
    public UsuarioDto findByUsuario(String usuario) {
        return usuarioMapper.toUsuarioDto(usuarioRepository.findByUsuario(usuario));
    }

    @Override
    public UsuarioDto agregarUsuario(UsuarioDto usuarioDto) {
        return usuarioMapper.toUsuarioDto(usuarioRepository.save(usuarioMapper.toUsuario(usuarioDto)));
    }

}
