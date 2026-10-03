package com.ventanago.auth.repository;

import com.ventanago.auth.repository.entity.CodigoAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CodigoAccesoRepository extends JpaRepository<CodigoAcceso, Long> {

    long countByEmailAndCreadoAfter(String email, LocalDateTime desde);

    Optional<CodigoAcceso> findFirstByEmailAndUsadoFalseOrderByCreadoDesc(String email);
}
