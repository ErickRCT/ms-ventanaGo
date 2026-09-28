package com.ventanago.pauta.repository;

import com.ventanago.pauta.repository.entity.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PautaRepository extends JpaRepository<Pauta, Long> {

    List<Pauta> findBySerie_SerieId(Long idSerie);



}
