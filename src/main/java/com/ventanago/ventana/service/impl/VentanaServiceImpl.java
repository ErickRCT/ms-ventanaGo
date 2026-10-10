package com.ventanago.ventana.service.impl;

import com.ventanago.cotizacion.service.CotizacionService;
import com.ventanago.pauta.service.dto.PautaDto;
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

    private final CotizacionService cotizacionService;

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
            validarDatos(ventanaDto);
            calcularValorVentana(ventanaDto);
            Ventana guardada = ventanaRepository.saveAndFlush(ventanaMapper.toEntity(ventanaDto));
            if (ventanaDto.getCotizacionId() != null) cotizacionService.recalcularTotales(ventanaDto.getCotizacionId());
            return ventanaMapper.toDto(guardada);
        }
        if(ventanaRepository.existsById(ventanaDto.getVentanaId())){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    }

    // Calcula el precio neto de la ventana sin guardarla.
    @Override
    public VentanaDto cotizarVentana(VentanaDto ventanaDto) {
        validarDatos(ventanaDto);
        calcularValorVentana(ventanaDto);
        return ventanaDto;
    }

    @Transactional
    @Override
    public boolean eliminarVentana(Long id){
        Ventana ventana = ventanaRepository.findById(id).orElse(null);
        if (ventana != null) {
            Long cotizacionId = ventana.getCotizacion() == null ? null : ventana.getCotizacion().getCotizacionId();
            ventanaRepository.delete(ventana);
            ventanaRepository.flush();
            if (cotizacionId != null) cotizacionService.recalcularTotales(cotizacionId);
            return true;
        } else {
            return false;
        }
    }

    // Datos incompletos son un error del que llama (400), no del servidor.
    private static void validarDatos(VentanaDto ventanaDto) {
        if (ventanaDto.getPauta() == null || ventanaDto.getColor() == null || ventanaDto.getColor().getValor() == null
                || ventanaDto.getVidrio() == null || ventanaDto.getAncho() <= 0 || ventanaDto.getAlto() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan la pauta, el color, el vidrio o las medidas.");
        }
    }

    private void calcularValorVentana(VentanaDto ventanaDto) {
        ventanaDto.setPrecioNeto(calculaValorPauta(ventanaDto));
    }

    private int calculaValorPauta(VentanaDto ventanaDto) {
        double valorPerfileria = getPesoTeoricoCompleto(ventanaDto) * ventanaDto.getColor().getValor();
        double valorQuincalleria = getValorQuincalleria(ventanaDto);
        double valorVidrio = getValorVidrio(ventanaDto);
        return (int) Math.round(valorPerfileria + valorQuincalleria + valorVidrio);
    }

    /** Kilos de aluminio: peso teórico (kg por metro de ancho / de alto) por las medidas en metros. */
    private static double getPesoTeoricoCompleto(VentanaDto ventanaDto) {
        PautaDto pauta = ventanaDto.getPauta();
        boolean reforzadaH = Boolean.TRUE.equals(pauta.getIsReforzada()) && ventanaDto.getAncho() >= pauta.getHorizontalReforzada();
        boolean reforzadaV = Boolean.TRUE.equals(pauta.getIsReforzada()) && ventanaDto.getAlto() >= pauta.getVerticalReforzada();

        double pesoHorizontal = pesoTeorico(pauta, 'H', pauta.getPesoTeoricoHorizontal());
        double pesoVertical = pesoTeorico(pauta, 'V', pauta.getPesoTeoricoVertical());
        // Desde las medidas de refuerzo se usa el peso reforzado, si la pauta lo tiene cargado.
        if (reforzadaH && positivo(pauta.getPesoTeoricoReforzadoHorizontal())) {
            pesoHorizontal = pauta.getPesoTeoricoReforzadoHorizontal();
        }
        if (reforzadaV && positivo(pauta.getPesoTeoricoReforzadoVertical())) {
            pesoVertical = pauta.getPesoTeoricoReforzadoVertical();
        }
        return pesoHorizontal * ventanaDto.getAncho() / 1000.0 + pesoVertical * ventanaDto.getAlto() / 1000.0;
    }

    /**
     * Peso teórico cargado en la pauta; si viene en 0 se calcula igual que se cargan a mano:
     * suma de peso (kg/m) × cantidad de los perfiles de esa orientación.
     */
    private static double pesoTeorico(PautaDto pauta, char orientacion, Double pesoCargado) {
        if (positivo(pesoCargado)) return pesoCargado;
        if (pauta.getPerfiles() == null) return 0;
        return pauta.getPerfiles().stream()
                .filter(p -> Character.toUpperCase(p.getOrientacion()) == orientacion && p.getPerfil() != null)
                .mapToDouble(p -> pesoPerfil(p.getPerfil().getPeso()) * (p.getCantidad() == null ? 1 : p.getCantidad()))
                .sum();
    }

    private static double pesoPerfil(String peso) {
        if (peso == null || peso.isBlank()) return 0;
        try {
            return Double.parseDouble(peso.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean positivo(Double valor) {
        return valor != null && valor > 0;
    }

    /** "Mt": metros según variacionH × ancho + variacionV × alto; el resto: valor por pieza × cantidad. */
    private static double getValorQuincalleria(VentanaDto ventanaDto) {
        if (ventanaDto.getPauta().getQuincallerias() == null) return 0;
        return ventanaDto.getPauta().getQuincallerias().stream()
                .mapToDouble(obj -> {
                    double valor = obj.getQuincalleria().getValor();
                    if ("Mt".equalsIgnoreCase(obj.getQuincalleria().getUnidad())) {
                        double metros = (ventanaDto.getAncho() * obj.getVariacionH() + ventanaDto.getAlto() * obj.getVariacionV()) / 1000.0;
                        return metros * valor;
                    }
                    return obj.getCantidad() * valor;
                }).sum();
    }

    /**
     * Las pautas son correderas de 2 hojas: cada línea de vidrio es un paño por hoja, de
     * (ancho / 2 - variacionH) × (alto - variacionV), y se multiplica por su cantidad y por las 2 hojas.
     * Las variaciones son descuentos en mm respecto de la medida de la ventana.
     */
    private static double getValorVidrio(VentanaDto ventanaDto) {
        if (ventanaDto.getPauta().getVidrios() == null) return 0;
        double metrosCuadrados = ventanaDto.getPauta().getVidrios().stream()
                .mapToDouble(obj -> {
                    double ancho = Math.max(0, ventanaDto.getAncho() / 2.0 - valorONulo(obj.getVariacionH())) / 1000.0;
                    double alto = Math.max(0, ventanaDto.getAlto() - valorONulo(obj.getVariacionV())) / 1000.0;
                    long cantidad = obj.getCantidad() == null ? 1 : obj.getCantidad();
                    return ancho * alto * cantidad * 2;
                }).sum();
        return metrosCuadrados * ventanaDto.getVidrio().getValor();
    }

    private static long valorONulo(Long valor) {
        return valor == null ? 0 : valor;
    }

}
