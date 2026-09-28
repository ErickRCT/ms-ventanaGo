package com.ventanago;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ventanago")
public class MsVentanaGoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsVentanaGoApplication.class, args);
    }

}
