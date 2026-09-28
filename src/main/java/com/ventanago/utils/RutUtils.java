package com.ventanago.utils;

import org.mapstruct.Named;

public class RutUtils {


    @Named("formatearRut")
    public static String formatearRut(String rut) {
        // Eliminar puntos y guion si existen
        rut = rut.replaceAll("[^0-9kK]", "");

        // Separar número y dígito verificador
        String numero = rut.substring(0, rut.length() - 1);
        String dv = rut.substring(rut.length() - 1);

        // Agregar puntos cada tres dígitos desde la derecha
        StringBuilder rutFormateado = new StringBuilder();
        int contador = 0;
        for (int i = numero.length() - 1; i >= 0; i--) {
            rutFormateado.insert(0, numero.charAt(i));
            contador++;
            if (contador == 3 && i != 0) {
                rutFormateado.insert(0, ".");
                contador = 0;
            }
        }

        // Agregar guion y dígito verificador
        rutFormateado.append("-").append(dv);

        return rutFormateado.toString();
    }
}
