package com.ventanago.reporteria.service.impl;

import com.ventanago.reporteria.pdf.PdfCotizacion;
import com.ventanago.reporteria.pdf.PdfOrdenDeTrabajo;
import com.ventanago.reporteria.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final PdfCotizacion pdfCotizacion;
    private final PdfOrdenDeTrabajo pdfOrdenDeTrabajo;

    @Override
    public byte[] generarPdfCotizacion(Long id) throws IOException {
        return pdfCotizacion.generarPdfCotizacion(id);
    }

    @Override
    public byte[] generarPdfOrdenDeTrabajo(Long id) throws IOException {
        return pdfOrdenDeTrabajo.generarPdfOrdenDeTrabajo(id);
    }

}
