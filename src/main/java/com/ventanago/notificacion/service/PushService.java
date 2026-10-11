package com.ventanago.notificacion.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.ventanago.notificacion.repository.DispositivoPushRepository;
import com.ventanago.notificacion.repository.entity.DispositivoPush;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notificaciones push con Firebase Cloud Messaging (API HTTP v1). Cada aviso que se guarda (oferta nueva, mensaje
 * del chat, oferta elegida…) llega también al teléfono de la cuenta, aunque la app esté cerrada.
 * Sin la variable FIREBASE_CREDENCIALES (JSON de la cuenta de servicio, tal cual o en base64) los avisos siguen
 * funcionando dentro de la app, pero no se envían push.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private static final String ALCANCE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final int MAX_TEXTO = 1000;

    private final DispositivoPushRepository dispositivoRepository;
    private final RestClient http = RestClient.create();

    @Value("${app.push.credenciales:}")
    private String credencialesTexto;

    private GoogleCredentials credenciales;
    private String urlEnvio;

    @PostConstruct
    void iniciar() {
        if (credencialesTexto == null || credencialesTexto.isBlank()) {
            log.warn("FIREBASE_CREDENCIALES no está configurado: los avisos NO se envían como notificación push.");
            return;
        }
        String json = credencialesTexto.trim();
        if (!json.startsWith("{")) json = new String(Base64.getDecoder().decode(json), StandardCharsets.UTF_8);
        try {
            GoogleCredentials leidas = GoogleCredentials.fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
            if (!(leidas instanceof ServiceAccountCredentials cuenta) || cuenta.getProjectId() == null) {
                log.error("FIREBASE_CREDENCIALES no es el JSON de una cuenta de servicio de Firebase: no se envían push.");
                return;
            }
            credenciales = leidas.createScoped(List.of(ALCANCE));
            urlEnvio = "https://fcm.googleapis.com/v1/projects/" + cuenta.getProjectId() + "/messages:send";
            log.info("Notificaciones push activas (proyecto Firebase {}).", cuenta.getProjectId());
        } catch (IOException | IllegalArgumentException e) {
            log.error("No se pudo leer FIREBASE_CREDENCIALES: no se envían push.", e);
        }
    }

    /** Asocia el token del teléfono a la cuenta (si el teléfono era de otra cuenta, deja de recibir sus avisos). */
    @Transactional
    public void registrar(Long cuentaId, String token) {
        DispositivoPush dispositivo = dispositivoRepository.findByToken(token).orElseGet(DispositivoPush::new);
        dispositivo.setToken(token);
        dispositivo.setCuentaId(cuentaId);
        dispositivo.setActualizado(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);
    }

    /** Al cerrar sesión el teléfono deja de recibir los avisos de la cuenta. */
    public void quitar(Long cuentaId, String token) {
        dispositivoRepository.eliminarTokenDeCuenta(token, cuentaId);
    }

    /** Se envía después de confirmar la transacción que guardó el aviso, fuera del hilo de la petición. */
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void alCrearAviso(AvisoCreado aviso) {
        if (credenciales == null || aviso.cuentaId() == null) return;
        List<DispositivoPush> dispositivos = dispositivoRepository.findByCuentaId(aviso.cuentaId());
        if (dispositivos.isEmpty()) return;
        String acceso;
        try {
            acceso = tokenDeAcceso();
        } catch (IOException e) {
            log.error("No se pudo obtener el token de acceso de Firebase", e);
            return;
        }
        for (DispositivoPush d : dispositivos) enviar(acceso, d.getToken(), aviso);
    }

    private synchronized String tokenDeAcceso() throws IOException {
        credenciales.refreshIfExpired();
        return credenciales.getAccessToken().getTokenValue();
    }

    private void enviar(String acceso, String token, AvisoCreado aviso) {
        // Solo "data": la app arma la notificación (canal, ícono y a qué pantalla lleva) también con la app cerrada.
        Map<String, String> datos = new HashMap<>();
        datos.put("titulo", recortar(aviso.titulo()));
        datos.put("mensaje", recortar(aviso.mensaje() == null ? "" : aviso.mensaje()));
        if (aviso.solicitudNumero() != null) datos.put("solicitud", String.valueOf(aviso.solicitudNumero()));
        Map<String, Object> mensaje = Map.of(
                "token", token,
                "data", datos,
                "android", Map.of("priority", "HIGH", "ttl", "86400s"));
        try {
            http.post()
                    .uri(urlEnvio)
                    .header("Authorization", "Bearer " + acceso)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", mensaje))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            String cuerpo = e.getResponseBodyAsString();
            // Token de una app desinstalada o vencido: no se vuelve a intentar.
            if (e.getStatusCode().value() == 404 || cuerpo.contains("UNREGISTERED") || cuerpo.contains("INVALID_ARGUMENT")) {
                dispositivoRepository.eliminarToken(token);
            } else {
                log.warn("FCM rechazó la notificación ({}): {}", e.getStatusCode(), cuerpo);
            }
        } catch (RestClientException e) {
            log.warn("No se pudo enviar la notificación push", e);
        }
    }

    private static String recortar(String texto) {
        return texto.length() > MAX_TEXTO ? texto.substring(0, MAX_TEXTO) : texto;
    }
}
