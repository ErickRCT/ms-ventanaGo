package com.ventanago.solicitud.repository;

import com.ventanago.solicitud.repository.entity.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    List<Aviso> findTop50ByDestinatarioOrderByFechaDesc(String destinatario);

    @Modifying
    @Query("update Aviso a set a.leido = true where a.destinatario = :destinatario and a.leido = false")
    int marcarLeidos(@Param("destinatario") String destinatario);
}
