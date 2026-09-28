package com.ventanago.reporteria.util;

import com.ventanago.cliente.service.dto.ClienteDto;
import com.ventanago.comuna.service.dto.ComunaDto;
import com.ventanago.region.service.dto.RegionDto;

import static org.apache.logging.log4j.util.Strings.EMPTY;

public class ReporteriaUtils {

    public static ClienteDto obtenerCliente(ClienteDto cliente){
        if(cliente == null){
            return ClienteDto.builder()
                    .rut("SIN RUT")
                    .email("SIN EMAIL")
                    .telefono("SIN TELÉFONO")
                    .nombre("")
                    .comuna(ComunaDto.builder()
                            .nombre(EMPTY)
                            .region(RegionDto.builder()
                                    .nombre(EMPTY).build())
                            .build())
                    .build();
        }
        return cliente;
    }

}
