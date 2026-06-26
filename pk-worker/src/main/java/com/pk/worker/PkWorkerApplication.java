package com.pk.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.pk")
@EnableScheduling
public class PkWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PkWorkerApplication.class, args);
    }
}
