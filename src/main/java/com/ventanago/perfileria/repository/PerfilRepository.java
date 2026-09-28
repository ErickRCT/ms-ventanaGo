package com.ventanago.perfileria.repository;

import com.ventanago.perfileria.repository.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    List<Perfil> findBySerie_SerieId(Long serieId);

    List<Perfil> findByTipoPerfil_TipoPerfilId(Long tipoPerfilId);

}
