package com.ventanago.login.service.mapper;

import com.ventanago.login.repository.entity.Usuario;
import com.ventanago.login.service.dto.UsuarioDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioDto toUsuarioDto(Usuario usuario);

    Usuario toUsuario(UsuarioDto usuarioDto);

    List<UsuarioDto> toUsuarioDtoList(List<Usuario> usuarios);
    List<Usuario> toUsuarioList(List<UsuarioDto> usuarioDtoList);

}
