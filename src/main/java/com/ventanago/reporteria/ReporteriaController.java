package com.ventanago.reporteria;

import com.ventanago.reporteria.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
public class ReporteriaController {

    private final PdfService pdfService;

    @GetMapping("/cotizacion/{id}")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) throws IOException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=COTIZACION_"+id+"+.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfService.generarPdfCotizacion(id));
    }

    @GetMapping ("/orden-de-trabajo/{id}")
    ResponseEntity<byte[]> generarPdfOrdenDeTrabajo(@PathVariable Long id) throws IOException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ORDEN_DE_TRABAJO_"+id+"+.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfService.generarPdfOrdenDeTrabajo(id));
    }

    @GetMapping("/optimizacion/{id}")
    public ResponseEntity<byte[]> generarPdfOptimizacion(@PathVariable Long id) throws IOException {
        return null;
    }


}
