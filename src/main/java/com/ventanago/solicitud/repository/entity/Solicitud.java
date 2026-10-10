package com.ventanago.solicitud.repository.entity;

import com.ventanago.auth.repository.entity.Cuenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Solicitud de cotización que el cliente envía desde su carrito (o describiendo un trabajo sin ventanas).
 * Llega a los proveedores de su comuna; cada uno puede enviar una oferta y el cliente elige una.
 */
@Getter
@Setter
@Entity
@Table(name = "solicitud")
public class Solicitud {

    /**
     * PENDIENTE: abierta, recibiendo ofertas. ADJUDICADA: el cliente eligió una oferta. TERMINADA: el cliente valoró
     * el trabajo. CANCELADA: el cliente la retiró.
     * ACEPTADA, MODIFICADA y RECHAZADA son del esquema anterior (una sola respuesta por solicitud);
     * MigracionOfertas las convierte en ofertas al arrancar.
     */
    public enum Estado { PENDIENTE, ADJUDICADA, TERMINADA, CANCELADA, ACEPTADA, MODIFICADA, RECHAZADA }

    /** Los cuatro últimos no necesitan ventanas diseñadas: el cliente describe el trabajo. */
    public enum Servicio { FABRICACION, INSTALACION, FLETE, REPARACION, CAMBIO_VIDRIO, MANTENCION, MEDICION }

    /** Servicios que se pueden pedir sin ventanas en el carrito. */
    public static final Set<Servicio> SIN_VENTANAS = Set.of(Servicio.REPARACION, Servicio.CAMBIO_VIDRIO, Servicio.MANTENCION, Servicio.MEDICION);

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

    /** Dónde es el trabajo; decide qué proveedores reciben la solicitud. Null en solicitudes antiguas. */
    @Column(name = "comuna_id")
    private Long comunaId;

    /** Proveedores elegidos por el cliente. Vacío: todos los que cubren la comuna y los servicios. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "solicitud_invitado", joinColumns = @JoinColumn(name = "numero"))
    @Column(name = "proveedor_id")
    private Set<Long> invitados = new LinkedHashSet<>();

    @OneToMany(mappedBy = "solicitud")
    @OrderBy("ofertaId ASC")
    @BatchSize(size = 50)
    private List<Oferta> ofertas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado estado = Estado.PENDIENTE;

    /** Ventanas vigentes y, si la solicitud se modificó, también las que pidió el cliente (original = true). */
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("solicitudItemId ASC")
    private List<SolicitudItem> items = new ArrayList<>();

    // ----- Proveedor elegido: copia de la oferta elegida (en el esquema anterior, la única respuesta) -----

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
