package com.ventanago.solicitud.repository;

import com.ventanago.solicitud.repository.entity.MensajeOferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MensajeOfertaRepository extends JpaRepository<MensajeOferta, Long> {

    List<MensajeOferta> findByOfertaOfertaIdOrderByFechaAscMensajeIdAsc(Long ofertaId);

    /** [ofertaId, cantidad] de mensajes de cada oferta. */
    @Query("select m.oferta.ofertaId, count(m) from MensajeOferta m where m.oferta.ofertaId in :ids group by m.oferta.ofertaId")
    List<Object[]> contarPorOferta(@Param("ids") Collection<Long> ids);
}
