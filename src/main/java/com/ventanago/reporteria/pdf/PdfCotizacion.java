package com.ventanago.reporteria.pdf;


import com.ventanago.cliente.service.dto.ClienteDto;
import com.ventanago.comuna.service.dto.ComunaDto;
import com.ventanago.cotizacion.service.CotizacionService;
import com.ventanago.cotizacion.service.dto.CotizacionDto;
import com.ventanago.region.service.dto.RegionDto;
import com.ventanago.reporteria.util.ReporteriaUtils;
import com.ventanago.utils.RutUtils;
import com.ventanago.ventana.service.dto.VentanaDto;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
        HtmlConverter.convertToPdf(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), new ByteArrayOutputStream());
        renderer.setDocumentFromString(html);
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
        data.put("m2Total", calcularTotalM2Cotizacion(cotizacionDto.getVentanas()));
        return data;
    }

    private double calcularTotalM2Cotizacion(Set<VentanaDto> ventanas){
           return ventanas.stream()
                   .mapToDouble(ventana -> ((double) ventana.getAncho() / 1000) * ((double) ventana.getAlto() / 1000))
                   .sum();
    }


}
