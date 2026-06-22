package com.pk.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.pk")
public class PkWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PkWorkerApplication.class, args);
    }
}
