package com.ventanago.notificacion.repository;

import com.ventanago.notificacion.repository.entity.DispositivoPush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DispositivoPushRepository extends JpaRepository<DispositivoPush, Long> {

    List<DispositivoPush> findByCuentaId(Long cuentaId);

    Optional<DispositivoPush> findByToken(String token);

    @Transactional
    @Modifying
    @Query("delete from DispositivoPush d where d.token = :token")
    int eliminarToken(@Param("token") String token);

    @Transactional
    @Modifying
    @Query("delete from DispositivoPush d where d.token = :token and d.cuentaId = :cuentaId")
    int eliminarTokenDeCuenta(@Param("token") String token, @Param("cuentaId") Long cuentaId);
}
