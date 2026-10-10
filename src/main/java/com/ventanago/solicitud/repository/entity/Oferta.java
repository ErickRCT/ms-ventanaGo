package com.ventanago.solicitud.repository.entity;

import com.ventanago.auth.repository.entity.Cuenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de un proveedor a una solicitud. Cada solicitud puede recibir una oferta por proveedor;
 * el cliente compara y elige una (ELEGIDA), y las demás quedan DESCARTADAS.
 */
@Getter
@Setter
@Entity
@Table(name = "oferta", uniqueConstraints = @UniqueConstraint(name = "uk_oferta_proveedor", columnNames = {"numero", "proveedor_id"}))
public class Oferta {

    /** Lo que propone el proveedor: lo pedido con precio, lo pedido con cambios, o no tomar el trabajo. */
    public enum Tipo { ACEPTADA, MODIFICADA, RECHAZADA }

    public enum Estado { VIGENTE, ELEGIDA, DESCARTADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oferta_id")
    private Long ofertaId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "numero", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Cuenta proveedor;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado estado = Estado.VIGENTE;

    @Column(name = "mensaje", length = 2000)
    private String mensaje;

    /** Total en pesos; null si el proveedor no toma el trabajo. */
    @Column(name = "total")
    private Long total;

    /** Días hábiles estimados para terminar el trabajo. */
    @Column(name = "plazo_dias")
    private Integer plazoDias;

    @Column(name = "notificado_en_app", nullable = false)
    private boolean notificadoEnApp;

    /** Ventanas con el precio (y las medidas, si el proveedor las cambió). Vacía en solicitudes sin ventanas. */
    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ofertaItemId ASC")
    private List<OfertaItem> items = new ArrayList<>();
}
