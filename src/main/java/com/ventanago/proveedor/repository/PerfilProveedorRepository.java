package com.ventanago.proveedor.repository;

import com.ventanago.proveedor.repository.entity.PerfilProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilProveedorRepository extends JpaRepository<PerfilProveedor, Long> {
}
