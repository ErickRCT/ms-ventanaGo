package com.ventanago.ventana.service.impl;

import com.ventanago.ventana.repository.VentanaRepository;
import com.ventanago.ventana.repository.entity.Ventana;
import com.ventanago.ventana.service.VentanaService;
import com.ventanago.ventana.service.dto.VentanaDto;
import com.ventanago.ventana.service.mapper.VentanaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VentanaServiceImpl implements VentanaService {

    private final VentanaRepository ventanaRepository;

    private final VentanaMapper ventanaMapper;

    @Override
    public List<VentanaDto> obtenerVentanas(){
        List<Ventana> ventanas = ventanaRepository.findAll();
        return ventanaMapper.toDtoList(ventanas);
    }

    @Override
    public VentanaDto obtenerVentana(Long id){
        if(ventanaRepository.findById(id).isPresent()){
            return ventanaMapper.toDto(ventanaRepository.findById(id).get());
        }else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    @Override
    public VentanaDto cotizarYGuardarVentana(VentanaDto ventanaDto) {
        if(ventanaDto.getVentanaId() == null){
            calcularValorVentana(ventanaDto);
            return ventanaMapper.toDto(ventanaRepository.save(ventanaMapper.toEntity(ventanaDto)));
        }
        if(ventanaRepository.existsById(ventanaDto.getVentanaId())){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    }

    @Override
    public boolean eliminarVentana(Long id){
        if (ventanaRepository.existsById(id)) {
            ventanaRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    private void calcularValorVentana(VentanaDto ventanaDto) {
        ventanaDto.setPrecioNeto(calculaValorPauta(ventanaDto));
    }

    private int calculaValorPauta(VentanaDto ventanaDto) {

        Long valorAluminio = ventanaDto.getColor().getValor();
        double pesoTeoricoCompleto = getPesoTeoricoCompleto(ventanaDto);
        int valorPerfileria = (int) (pesoTeoricoCompleto * valorAluminio);
        int valorQuincalleria = ventanaDto.getPauta().getQuincallerias().stream()
                .mapToInt(obj -> {
                    if(obj.getQuincalleria().getUnidad().equals("Mt")){
                       return ((ventanaDto.getAncho() * obj.getVariacionH()) +
                               (ventanaDto.getAlto() * obj.getVariacionV()))/1000 * obj.getQuincalleria().getValor() ;
                    }
                    return obj.getCantidad() * obj.getQuincalleria().getValor();
                }).sum();
        int valorVidrio = getValorVidrio(ventanaDto);
        return valorQuincalleria + valorPerfileria + valorVidrio;

    }

    private static double getPesoTeoricoCompleto(VentanaDto ventanaDto) {
        double pesoTeoricoHorizontal = ventanaDto.getAncho() > ventanaDto.getPauta().getHorizontalReforzada() ?
                ventanaDto.getPauta().getPesoTeoricoHorizontal() : ventanaDto.getPauta().getPesoTeoricoReforzadoHorizontal();
        double pesoTeoricoVertical = ventanaDto.getAlto() > ventanaDto.getPauta().getVerticalReforzada() ?
                ventanaDto.getPauta().getPesoTeoricoVertical() : ventanaDto.getPauta().getPesoTeoricoReforzadoVertical();
        return (pesoTeoricoHorizontal * (ventanaDto.getAncho()/1000.0) ) + (pesoTeoricoVertical * ventanaDto.getAlto()/1000);
    }

    private static int getValorVidrio(VentanaDto ventanaDto){
        double anchoVidrio = ventanaDto.getPauta().getVidrios().stream()
                .mapToDouble(obj -> ((ventanaDto.getAncho() / 2.0) + obj.getVariacionH())).sum();
        double altoVidrio = ventanaDto.getPauta().getVidrios().stream().mapToDouble(obj ->
                ((ventanaDto.getAlto()) + obj.getVariacionH())).sum();
        double valorVidrio = (ventanaDto.getVidrio().getValor() * (anchoVidrio/1000 * altoVidrio/1000));
        return (int) valorVidrio;
    }

}
