package com.ventanago.login.repository;

import com.ventanago.login.repository.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

     Usuario findByUsuario(String usuario);



}
