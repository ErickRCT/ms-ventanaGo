package com.ventanago.solicitud.repository.entity;

import com.ventanago.auth.repository.entity.Cuenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Solicitud de cotización que el cliente envía desde su carrito y que responde un proveedor. */
@Getter
@Setter
@Entity
@Table(name = "solicitud")
public class Solicitud {

    public enum Estado { PENDIENTE, ACEPTADA, MODIFICADA, RECHAZADA }

    public enum Servicio { FABRICACION, INSTALACION, FLETE }

    /** Es el "N°" que ven cliente y empresa. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numero")
    private Long numero;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cliente;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "contacto_nombre", nullable = false)
    private String contactoNombre;

    @Column(name = "contacto_email", nullable = false)
    private String contactoEmail;

    @Column(name = "contacto_telefono", nullable = false)
    private String contactoTelefono;

    @Column(name = "contacto_direccion", nullable = false)
    private String contactoDireccion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "solicitud_servicio", joinColumns = @JoinColumn(name = "numero"))
    @Enumerated(EnumType.STRING)
    @Column(name = "servicio", length = 20)
    private Set<Servicio> servicios = new LinkedHashSet<>();

    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado estado = Estado.PENDIENTE;

    /** Ventanas vigentes y, si la solicitud se modificó, también las que pidió el cliente (original = true). */
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("solicitudItemId ASC")
    private List<SolicitudItem> items = new ArrayList<>();

    // ----- Respuesta del proveedor -----

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondida_por")
    private Cuenta respondidaPor;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(name = "mensaje_respuesta", length = 2000)
    private String mensajeRespuesta;

    /** Total en pesos; null si se rechazó. */
    @Column(name = "total")
    private Long total;

    @Column(name = "notificado_en_app")
    private Boolean notificadoEnApp;

    @Column(name = "notificado_por_correo")
    private Boolean notificadoPorCorreo;
}
