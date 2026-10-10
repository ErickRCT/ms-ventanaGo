package com.ventanago.solicitud;

import com.ventanago.solicitud.repository.OfertaRepository;
import com.ventanago.solicitud.repository.SolicitudRepository;
import com.ventanago.solicitud.repository.entity.*;
import com.ventanago.solicitud.repository.entity.Solicitud.Estado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Convierte las solicitudes del esquema anterior (una sola respuesta, guardada en la solicitud) en ofertas.
 * La respuesta pasa a ser la oferta de quien respondió, la solicitud vuelve a lo que pidió el cliente y queda abierta
 * para que el cliente elija (o para que otros proveedores oferten). Se ejecuta al arrancar y no hace nada si no
 * quedan solicitudes antiguas.
 */
@Slf4j
@Order(0)
@Component
public class MigracionOfertas implements CommandLineRunner {

    private static final List<Estado> ANTIGUOS = List.of(Estado.ACEPTADA, Estado.MODIFICADA, Estado.RECHAZADA);

    private final SolicitudRepository solicitudRepository;
    private final OfertaRepository ofertaRepository;
    private final TransactionTemplate transaccion;

    public MigracionOfertas(SolicitudRepository solicitudRepository, OfertaRepository ofertaRepository, PlatformTransactionManager tm) {
        this.solicitudRepository = solicitudRepository;
        this.ofertaRepository = ofertaRepository;
        this.transaccion = new TransactionTemplate(tm);
    }

    @Override
    public void run(String... args) {
        Integer migradas = transaccion.execute(estado -> migrar());
        if (migradas != null && migradas > 0) log.info("{} solicitud(es) antiguas convertidas a ofertas.", migradas);
    }

    private int migrar() {
        List<Solicitud> antiguas = solicitudRepository.findByEstadoIn(ANTIGUOS);
        for (Solicitud s : antiguas) {
            if (s.getRespondidaPor() != null && s.getOfertas().isEmpty()) {
                ofertaRepository.save(ofertaDe(s));
            }
            s.setEstado(Estado.PENDIENTE);
        }
        return antiguas.size();
    }

    private static Oferta ofertaDe(Solicitud s) {
        Oferta oferta = new Oferta();
        oferta.setSolicitud(s);
        oferta.setProveedor(s.getRespondidaPor());
        oferta.setFecha(s.getFechaRespuesta() != null ? s.getFechaRespuesta() : s.getFecha());
        oferta.setTipo(Oferta.Tipo.valueOf(s.getEstado().name()));
        oferta.setMensaje(s.getMensajeRespuesta());
        oferta.setNotificadoEnApp(Boolean.TRUE.equals(s.getNotificadoEnApp()));
        if (oferta.getTipo() == Oferta.Tipo.RECHAZADA) return oferta;

        List<SolicitudItem> vigentes = s.getItems().stream().filter(i -> !i.isOriginal()).toList();
        List<SolicitudItem> originales = s.getItems().stream().filter(SolicitudItem::isOriginal).toList();
        long total = 0;
        for (SolicitudItem item : vigentes) {
            if (item.getPrecioUnitario() == null) continue;
            OfertaItem copia = new OfertaItem();
            copia.setOferta(oferta);
            copia.setSolicitudItemId(item.getSolicitudItemId());
            copia.setVentana(item.getVentana().copia());
            copia.setPrecioUnitario(item.getPrecioUnitario());
            oferta.getItems().add(copia);
            total += item.getPrecioUnitario() * item.getVentana().getCantidad();
        }
        oferta.setTotal(s.getTotal() != null ? s.getTotal() : total);

        // La solicitud vuelve a lo que pidió el cliente: los cambios del proveedor quedan solo en su oferta.
        if (originales.size() == vigentes.size()) {
            for (int i = 0; i < vigentes.size(); i++) vigentes.get(i).setVentana(originales.get(i).getVentana().copia());
            s.getItems().removeAll(originales);
        }
        vigentes.forEach(i -> i.setPrecioUnitario(null));
        return oferta;
    }
}
