package com.ventanago.reporteria.pdf;

import com.ventanago.cliente.service.dto.ClienteDto;
import com.ventanago.color.service.dto.ColorDto;
import com.ventanago.comuna.service.dto.ComunaDto;
import com.ventanago.cotizacion.service.CalculoCotizacion;
import com.ventanago.cotizacion.service.CotizacionService;
import com.ventanago.cotizacion.service.dto.CotizacionDto;
import com.ventanago.pauta.service.dto.PautaDto;
import com.ventanago.region.service.dto.RegionDto;
import com.ventanago.serie.service.dto.SerieDto;
import com.ventanago.ventana.service.dto.VentanaDto;
import com.ventanago.vidrio.service.dto.VidrioDto;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Genera el PDF de cotización con datos de ejemplo (sin base de datos) y revisa los montos de venta. */
class PdfCotizacionTest {

    private static CotizacionDto cotizacionDeEjemplo() {
        SerieDto serie = SerieDto.builder().serieId(1L).nombre("AL-25").build();
        PautaDto pauta = PautaDto.builder().pautaId(1L).nombre("VENTANA CORREDERA").serie(serie).build();
        ColorDto color = ColorDto.builder().colorId(1L).nombre("Mate").valor(5200L).build();
        VidrioDto vidrio = VidrioDto.builder().vidrioId(1L).nombre("Templado").valor(1000).build();
        Set<VentanaDto> ventanas = new LinkedHashSet<>(List.of(
                VentanaDto.builder().ventanaId(1L).descripcion("Living").cantidad(2).ancho(1500).alto(1200)
                        .precioNeto(100_000).pauta(pauta).color(color).vidrio(vidrio).build(),
                VentanaDto.builder().ventanaId(2L).descripcion("Baño").cantidad(1).ancho(600).alto(400)
                        .precioNeto(40_000).pauta(pauta).color(color).vidrio(vidrio).build()));
        ClienteDto cliente = ClienteDto.builder().clienteId(1L).nombre("Cliente de prueba").rut("11.111.111-1")
                .telefono("+56911111111").email("prueba@ejemplo.cl").direccion("Calle 123")
                .comuna(ComunaDto.builder().nombre("Buin").region(RegionDto.builder().nombre("Metropolitana").build()).build())
                .build();
        return CotizacionDto.builder()
                .cotizacionId(99L).fecha(LocalDate.of(2026, 10, 9)).estado("CREADA")
                .ganancia(50).descuento(10)
                .flete("Buin").valorFlete(20_000).valorInstalacion(30_000).valorManoDeObra(0).valorOtrosGastos(0)
                .condiciones("50% de anticipo")
                .neto(0L)
                .cliente(cliente).ventanas(ventanas).build();
    }

    @Test
    void calculaElPrecioDeVenta() {
        CalculoCotizacion.Resumen resumen = CalculoCotizacion.resumen(cotizacionDeEjemplo());
        // factor = 1,5 × 0,9 = 1,35 → 135.000 × 2 + 54.000 × 1
        assertEquals(324_000, resumen.ventanas());
        assertEquals(50_000, resumen.extras());
        assertEquals(374_000, resumen.neto());
        assertEquals(71_060, resumen.iva());
        assertEquals(445_060, resumen.total());
    }

    @Test
    void generaElPdfConLaPlantilla() throws Exception {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine motor = new SpringTemplateEngine();
        motor.setTemplateResolver(resolver);

        CotizacionDto cotizacion = cotizacionDeEjemplo();
        CotizacionService servicio = new CotizacionService() {
            public List<CotizacionDto> obtenerCotizaciones() { return List.of(cotizacion); }
            public CotizacionDto obtenerCotizacion(Long id) { return cotizacion; }
            public CotizacionDto agregarCotizacion(CotizacionDto dto) { return dto; }
            public CotizacionDto modificarCotizacion(CotizacionDto dto) { return dto; }
            public void recalcularTotales(Long cotizacionId) { }
        };

        byte[] pdf = new PdfCotizacion(servicio, motor).generarPdfCotizacion(99L);
        assertTrue(pdf.length > 1000);
        Path salida = Path.of("target", "cotizacion-ejemplo.pdf");
        Files.createDirectories(salida.getParent());
        Files.write(salida, pdf);
    }
}
