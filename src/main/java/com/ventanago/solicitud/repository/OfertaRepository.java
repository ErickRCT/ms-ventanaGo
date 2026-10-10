package com.ventanago.solicitud.repository;

import com.ventanago.solicitud.repository.entity.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    /** Trabajos adjudicados por proveedor: [cuentaId, cantidad]. */
    @Query("select o.proveedor.cuentaId, count(o) from Oferta o where o.estado = com.ventanago.solicitud.repository.entity.Oferta.Estado.ELEGIDA group by o.proveedor.cuentaId")
    List<Object[]> trabajosPorProveedor();

    /** Precios ofrecidos por ventana, para el rango referencial: [precioUnitario, anchoMm, altoMm]. */
    @Query("select i.precioUnitario, i.ventana.anchoMm, i.ventana.altoMm from OfertaItem i where i.oferta.tipo <> com.ventanago.solicitud.repository.entity.Oferta.Tipo.RECHAZADA")
    List<Object[]> preciosOfrecidos();
}
