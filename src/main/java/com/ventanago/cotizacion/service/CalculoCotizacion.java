package com.ventanago.cotizacion.service;

import com.ventanago.cotizacion.service.dto.CotizacionDto;
import com.ventanago.ventana.service.dto.VentanaDto;

/**
 * Precio de venta de una cotización, el mismo que muestran la web y la app:
 * a cada ventana (precio de costo) se le aplica la ganancia y luego el descuento; se suman los
 * costos extras (flete, instalación, mano de obra y otros) y sobre ese neto se calcula el IVA.
 */
public final class CalculoCotizacion {

    public static final double IVA = 0.19;

    private CalculoCotizacion() {
    }

    public static double factorVenta(CotizacionDto cotizacion) {
        double ganancia = valor(cotizacion.getGanancia()) / 100.0;
        double descuento = valor(cotizacion.getDescuento()) / 100.0;
        return (1 + ganancia) * (1 - descuento);
    }

    /** Precio de venta de una unidad de la ventana. */
    public static long precioUnitario(VentanaDto ventana, double factorVenta) {
        return Math.round(ventana.getPrecioNeto() * factorVenta);
    }

    public static int cantidad(VentanaDto ventana) {
        return ventana.getCantidad() == null ? 1 : ventana.getCantidad();
    }

    public static Resumen resumen(CotizacionDto cotizacion) {
        double factor = factorVenta(cotizacion);
        long ventanas = cotizacion.getVentanas() == null ? 0 : cotizacion.getVentanas().stream()
                .mapToLong(v -> precioUnitario(v, factor) * cantidad(v))
                .sum();
        long extras = valor(cotizacion.getValorFlete()) + valor(cotizacion.getValorInstalacion())
                + valor(cotizacion.getValorManoDeObra()) + valor(cotizacion.getValorOtrosGastos());
        long neto = ventanas + extras;
        long iva = Math.round(neto * IVA);
        return new Resumen(ventanas, extras, neto, iva, neto + iva);
    }

    private static long valor(Integer numero) {
        return numero == null ? 0 : numero;
    }

    /** Montos en pesos: ventanas con ganancia y descuento, extras, neto, IVA y total. */
    public record Resumen(long ventanas, long extras, long neto, long iva, long total) {
    }
}
