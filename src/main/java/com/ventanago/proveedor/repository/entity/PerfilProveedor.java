package com.ventanago.proveedor.repository.entity;

import com.ventanago.auth.repository.entity.Cuenta;
import com.ventanago.solicitud.repository.entity.Solicitud.Servicio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Ficha pública de un proveedor: qué servicios presta y en qué comunas trabaja.
 * Las solicitudes nuevas llegan solo a los proveedores que cubren su comuna y alguno de sus servicios.
 */
@Getter
@Setter
@Entity
@Table(name = "perfil_proveedor")
public class PerfilProveedor {

    @Id
    @Column(name = "cuenta_id")
    private Long cuentaId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id")
    private Cuenta cuenta;

    @Column(name = "nombre_comercial", nullable = false)
    private String nombreComercial;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "anios_experiencia", nullable = false)
    private int aniosExperiencia;

    /** Lo marca el administrador después de revisar al proveedor (RUT, trabajos anteriores). */
    @Column(name = "verificado", nullable = false)
    private boolean verificado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_proveedor_servicio", joinColumns = @JoinColumn(name = "cuenta_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "servicio", length = 20)
    private Set<Servicio> servicios = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_proveedor_comuna", joinColumns = @JoinColumn(name = "cuenta_id"))
    @Column(name = "comuna_id")
    private Set<Long> comunas = new LinkedHashSet<>();
}
