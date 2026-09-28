package com.ventanago.cotizacion.repository;

import com.ventanago.cotizacion.repository.entity.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion,Long> {
}
