package com.ventanago.reporteria.pdf;

import com.ventanago.cotizacion.service.CotizacionService;
import com.ventanago.cotizacion.service.dto.CotizacionDto;
import com.ventanago.reporteria.util.ReporteriaUtils;
import com.ventanago.utils.RutUtils;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfOrdenDeTrabajo {

    private final CotizacionService cotizacionService;

    private final TemplateEngine templateEngine;

    public byte[] generarPdfOrdenDeTrabajo(Long id) throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        Map<String,Object> dataOrdenDeTrabajoPdf = generarDataPdfOrdenDeTrabajo(cotizacionService.obtenerCotizacion(id));
        Context context = new Context();
        context.setVariables(dataOrdenDeTrabajoPdf);
        String html = templateEngine.process("orden-de-trabajo",context);
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

    public Map<String,Object> generarDataPdfOrdenDeTrabajo(CotizacionDto cotizacionDto) throws IOException {
        Map<String, Object> data = new HashMap<>();
        cotizacionDto.setCliente(ReporteriaUtils.obtenerCliente(cotizacionDto.getCliente()));
        data.put("cotizacion", cotizacionDto);
        return data;
    }

}
