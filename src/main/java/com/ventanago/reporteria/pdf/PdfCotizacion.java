package com.ventanago.reporteria.pdf;


import com.ventanago.cliente.service.dto.ClienteDto;
import com.ventanago.comuna.service.dto.ComunaDto;
import com.ventanago.cotizacion.service.CalculoCotizacion;
import com.ventanago.cotizacion.service.CotizacionService;
import com.ventanago.cotizacion.service.dto.CotizacionDto;
import com.ventanago.region.service.dto.RegionDto;
import com.ventanago.reporteria.util.ReporteriaUtils;
import com.ventanago.utils.RutUtils;
import com.ventanago.ventana.service.dto.VentanaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.logging.log4j.util.Strings.EMPTY;

@Service
@RequiredArgsConstructor
public class PdfCotizacion {

    private final CotizacionService cotizacionService;

    private final TemplateEngine templateEngine;

    public byte[] generarPdfCotizacion(Long id) throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        Map<String,Object> dataCotizacionPdf = generarDataCotizacionPdf(cotizacionService.obtenerCotizacion(id));
        Context context = new Context();
        context.setVariables(dataCotizacionPdf);
        String html = templateEngine.process("cotizacion",context);
        // Base para resolver imágenes relativas (logo.png) desde resources/templates
        renderer.setDocumentFromString(html, new ClassPathResource("templates/").getURL().toExternalForm());
        renderer.getSharedContext().setPrint(true);
        renderer.getSharedContext().setInteractive(false);
        renderer.layout();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        }
    }

    private Map<String,Object> generarDataCotizacionPdf(CotizacionDto cotizacionDto) {
        Map<String, Object> data = new HashMap<>();
        cotizacionDto.setCliente(ReporteriaUtils.obtenerCliente(cotizacionDto.getCliente()));
        data.put("direccionCliente", cotizacionDto.getCliente().obtenerDireccionCompleta());
        data.put("cotizacion", cotizacionDto);

        // Precios de venta (con ganancia y descuento), calculados aquí y no en la plantilla:
        // así no dependen del neto guardado ni de divisiones enteras de Thymeleaf.
        double factor = CalculoCotizacion.factorVenta(cotizacionDto);
        Set<VentanaDto> ventanas = cotizacionDto.getVentanas() == null ? Set.of() : cotizacionDto.getVentanas();
        List<Map<String, Object>> lineas = new ArrayList<>();
        double m2Total = 0;
        int unidades = 0;
        for (VentanaDto ventana : ventanas) {
            int cantidad = CalculoCotizacion.cantidad(ventana);
            long unitario = CalculoCotizacion.precioUnitario(ventana, factor);
            double m2 = (ventana.getAncho() / 1000.0) * (ventana.getAlto() / 1000.0) * cantidad;
            m2Total += m2;
            unidades += cantidad;
            Map<String, Object> linea = new HashMap<>();
            linea.put("ventana", ventana);
            linea.put("m2", m2);
            linea.put("precioUnitario", unitario);
            linea.put("totalItem", unitario * cantidad);
            lineas.add(linea);
        }
        data.put("lineas", lineas);
        data.put("m2Total", m2Total);
        data.put("unidades", unidades);
        data.put("resumen", CalculoCotizacion.resumen(cotizacionDto));
        return data;
    }


}
