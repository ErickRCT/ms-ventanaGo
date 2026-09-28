package com.ventanago.reporteria.service;

import java.io.IOException;

public interface PdfService {

    byte[] generarPdfCotizacion(Long id) throws IOException;

    byte[] generarPdfOrdenDeTrabajo(Long id) throws IOException;
}
