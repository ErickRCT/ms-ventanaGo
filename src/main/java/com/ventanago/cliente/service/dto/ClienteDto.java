package com.ventanago.cliente.service.dto;

import com.ventanago.comuna.service.dto.ComunaDto;
import lombok.*;

import static org.apache.logging.log4j.util.Strings.EMPTY;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClienteDto {

    private Long clienteId;

    private String rut;

    private String nombre;

    private String telefono;

    private String email;

    private String direccion;

    private ComunaDto comuna;

    public String obtenerDireccionCompleta(){
        if( this.direccion == null){
            return EMPTY;
        }
        return this.direccion + ", " + this.comuna.getNombre() + ", " + this.comuna.getRegion().getNombre();
    }

}
