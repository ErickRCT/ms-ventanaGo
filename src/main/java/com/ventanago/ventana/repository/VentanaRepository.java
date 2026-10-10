package com.ventanago.ventana.repository;

import com.ventanago.ventana.repository.entity.Ventana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentanaRepository extends JpaRepository<Ventana, Long> {

    List<Ventana> findByCotizacion_CotizacionId(Long cotizacionId);
}
