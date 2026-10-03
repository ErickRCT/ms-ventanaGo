package com.ventanago.solicitud.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/** Ventana tal como la diseña el cliente; se comparte entre el carrito y las solicitudes. Medidas en milímetros. */
@Getter
@Setter
@Embeddable
public class DatosVentana {

    /** Nombre de la pauta elegida. */
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "pauta_id")
    private Long pautaId;

    @Column(name = "serie_nombre")
    private String serieNombre;

    @Column(name = "imagen_pauta")
    private String imagenPauta;

    /** Solo para dibujar la ventana. */
    @Column(name = "hojas", nullable = false)
    private int hojas;

    @Column(name = "ancho_mm", nullable = false)
    private int anchoMm;

    @Column(name = "alto_mm", nullable = false)
    private int altoMm;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "color_id")
    private Long colorId;

    @Column(name = "color_nombre", nullable = false)
    private String colorNombre;

    @Column(name = "vidrio_id")
    private Long vidrioId;

    @Column(name = "vidrio_nombre", nullable = false)
    private String vidrioNombre;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    public DatosVentana copia() {
        DatosVentana copia = new DatosVentana();
        copia.descripcion = descripcion;
        copia.pautaId = pautaId;
        copia.serieNombre = serieNombre;
        copia.imagenPauta = imagenPauta;
        copia.hojas = hojas;
        copia.anchoMm = anchoMm;
        copia.altoMm = altoMm;
        copia.cantidad = cantidad;
        copia.colorId = colorId;
        copia.colorNombre = colorNombre;
        copia.vidrioId = vidrioId;
        copia.vidrioNombre = vidrioNombre;
        copia.observaciones = observaciones;
        return copia;
    }
}
