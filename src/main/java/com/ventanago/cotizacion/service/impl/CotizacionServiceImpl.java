package com.ventanago.cotizacion.service.impl;

import com.ventanago.cotizacion.repository.CotizacionRepository;
import com.ventanago.cotizacion.repository.entity.Cotizacion;
import com.ventanago.cotizacion.service.CotizacionService;
import com.ventanago.cotizacion.service.dto.CotizacionDto;
import com.ventanago.cotizacion.service.mapper.CotizacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ventanago.ventana.repository.VentanaRepository;
import com.ventanago.ventana.repository.entity.Ventana;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl implements CotizacionService {

    private final CotizacionRepository cotizacionRepository;

    private final CotizacionMapper cotizacionMapper;

    private final VentanaRepository ventanaRepository;

    @Transactional
    @Override
    public List<CotizacionDto> obtenerCotizaciones(){
        List<Cotizacion> cotizaciones = cotizacionRepository.findAll();
        return cotizacionMapper.toDtoList(cotizaciones);
    }

    @Transactional
    @Override
    public CotizacionDto obtenerCotizacion(Long id){
        if (cotizacionRepository.findById(id).isPresent()){
            return cotizacionMapper.toDto(cotizacionRepository.findById(id).get());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public CotizacionDto agregarCotizacion(CotizacionDto cotizacionDto){
        if (cotizacionDto.getCotizacionId() == null){
            Cotizacion cotizacion = cotizacionMapper.toEntity(cotizacionDto);
            // Una cotización nueva aún no tiene ventanas: los totales parten en cero.
            cotizacion.setNeto(0L);
            cotizacion.setTotalm2(BigDecimal.ZERO);
            cotizacion.setCantidadProductos(0);
            return cotizacionMapper.toDto(cotizacionRepository.save(cotizacion));
        }
        if (cotizacionRepository.findById(cotizacionDto.getCotizacionId()).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public CotizacionDto modificarCotizacion(CotizacionDto cotizacionDto){
        if (cotizacionDto.getCotizacionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el id de la cotización.");
        }
        if(cotizacionRepository.existsById(cotizacionDto.getCotizacionId())){
            Cotizacion guardada = cotizacionRepository.save(cotizacionMapper.toEntity(cotizacionDto));
            // El neto que envía el front se ignora: siempre sale de las ventanas guardadas.
            recalcularTotales(guardada.getCotizacionId());
            return cotizacionMapper.toDto(guardada);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public void recalcularTotales(Long cotizacionId) {
        Cotizacion cotizacion = cotizacionRepository.findById(cotizacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        long neto = 0;
        int unidades = 0;
        double metrosCuadrados = 0;
        for (Ventana ventana : ventanaRepository.findByCotizacion_CotizacionId(cotizacionId)) {
            int cantidad = ventana.getCantidad() == null ? 1 : ventana.getCantidad();
            neto += (long) ventana.getPrecioNeto() * cantidad;
            unidades += cantidad;
            double ancho = ventana.getAncho() == null ? 0 : ventana.getAncho() / 1000.0;
            double alto = ventana.getAlto() == null ? 0 : ventana.getAlto() / 1000.0;
            metrosCuadrados += ancho * alto * cantidad;
        }
        cotizacion.setNeto(neto);
        cotizacion.setCantidadProductos(unidades);
        cotizacion.setTotalm2(BigDecimal.valueOf(metrosCuadrados).setScale(2, RoundingMode.HALF_UP));
    }
}
