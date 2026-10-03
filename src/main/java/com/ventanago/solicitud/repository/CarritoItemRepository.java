package com.ventanago.solicitud.repository;

import com.ventanago.solicitud.repository.entity.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    List<CarritoItem> findByCuentaCuentaIdOrderByCarritoItemIdAsc(Long cuentaId);

    Optional<CarritoItem> findByCarritoItemIdAndCuentaCuentaId(Long carritoItemId, Long cuentaId);

    void deleteByCuentaCuentaId(Long cuentaId);
}
