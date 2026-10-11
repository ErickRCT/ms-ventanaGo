package com.ventanago;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // Notificaciones push fuera del hilo de la petición (PushService).
@SpringBootApplication(scanBasePackages = "com.ventanago")
public class MsVentanaGoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsVentanaGoApplication.class, args);
    }

}
