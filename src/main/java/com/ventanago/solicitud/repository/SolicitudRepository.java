package com.ventanago.solicitud.repository;

import com.ventanago.solicitud.repository.entity.Solicitud;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    @EntityGraph(attributePaths = {"items", "cliente"})
    List<Solicitud> findAllByOrderByNumeroAsc();

    @EntityGraph(attributePaths = {"items", "cliente"})
    List<Solicitud> findByClienteCuentaIdOrderByNumeroAsc(Long cuentaId);

    @EntityGraph(attributePaths = {"items"})
    List<Solicitud> findByEstadoIn(Collection<Solicitud.Estado> estados);
}
