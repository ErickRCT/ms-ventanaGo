package com.ventanago.solicitud.repository;

import com.ventanago.solicitud.repository.entity.FotoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FotoSolicitudRepository extends JpaRepository<FotoSolicitud, Long> {

    @Query("select f.fotoId from FotoSolicitud f where f.solicitud.numero = :numero order by f.fotoId")
    List<Long> idsDeSolicitud(@Param("numero") Long numero);

    Optional<FotoSolicitud> findByFotoIdAndSolicitudNumero(Long fotoId, Long numero);
}
