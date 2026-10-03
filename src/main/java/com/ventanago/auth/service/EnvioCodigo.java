package com.ventanago.auth.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Envía el código de acceso por correo. Sin servidor de correo configurado (spring.mail.host)
 * el código solo se escribe en el log, para poder probar en local.
 * Punto de extensión para agregar WhatsApp más adelante.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnvioCodigo {

    private final ObjectProvider<JavaMailSender> mailSender;

    // Por defecto la misma cuenta SMTP: Gmail y la mayoría de proveedores rechazan otro remitente
    @Value("${app.mail.remitente:${spring.mail.username:no-responder@ventanago.cl}}")
    private String remitente;

    @PostConstruct
    void avisarSiNoHayCorreo() {
        if (mailSender.getIfAvailable() == null) {
            log.warn("spring.mail.host no está configurado: los códigos de acceso NO se envían por correo, solo se escriben en este log.");
        } else {
            log.info("Los códigos de acceso se enviarán por correo desde {}", remitente);
        }
    }

    public void enviar(String email, String codigo, int minutosVigencia) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Sin servidor de correo configurado. Código de acceso para {}: {}", email, codigo);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(email);
        mensaje.setSubject("Tu código para entrar a VentanaGo: " + codigo);
        mensaje.setText("Tu código de acceso es " + codigo + ".\n\n"
                + "Vence en " + minutosVigencia + " minutos. Si no lo pediste, ignora este correo.");
        try {
            sender.send(mensaje);
        } catch (MailException e) {
            log.error("No se pudo enviar el código de acceso a {}", email, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No pudimos enviar el código a tu correo. Intenta de nuevo en unos minutos.");
        }
    }
}
