package com.ventanago.auth.repository.entity;

/**
 * ADMIN: solo entra con credenciales, se crea por configuración (nunca por registro público).
 * CLIENTE: entra con Google o con un código enviado a su correo.
 * PROVEEDOR: empresa o particular (maestro/fletero) que presta servicios; entra con correo y contraseña.
 */
public enum Rol {
    ADMIN,
    CLIENTE,
    PROVEEDOR
}
