package com.ventanago.auth.repository;

import com.ventanago.auth.repository.entity.Cuenta;
import com.ventanago.auth.repository.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByEmail(String email);

    Optional<Cuenta> findByGoogleSub(String googleSub);

    boolean existsByRol(Rol rol);
}
