package com.ventanago.pauta.repository;

import com.ventanago.pauta.repository.entity.TipoPauta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoPautaRepository extends JpaRepository<TipoPauta, Long> {
}
