package com.ventanago.perfileria.repository;


import com.ventanago.perfileria.repository.entity.TipoPerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoPerfilRepository extends JpaRepository<TipoPerfil, Long> {
}
