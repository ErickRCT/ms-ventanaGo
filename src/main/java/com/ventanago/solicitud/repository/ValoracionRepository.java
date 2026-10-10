package com.ventanago.solicitud.repository;

import com.ventanago.solicitud.repository.entity.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    Optional<Valoracion> findBySolicitudNumero(Long numero);

    List<Valoracion> findTop30ByProveedorCuentaIdOrderByFechaDesc(Long proveedorId);

    /** [cuentaId, promedio, cantidad] de cada proveedor con valoraciones. */
    @Query("select v.proveedor.cuentaId, avg(v.estrellas), count(v) from Valoracion v group by v.proveedor.cuentaId")
    List<Object[]> resumenPorProveedor();
}
