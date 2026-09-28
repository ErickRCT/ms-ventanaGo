package com.ventanago.quincalleria.repository;

import com.ventanago.quincalleria.repository.entity.Quincalleria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuincalleriaRepository extends JpaRepository<Quincalleria, Long> {

    List<Quincalleria> findBySerie_SerieIdOrSerieIsNull(Long serieId);

}
