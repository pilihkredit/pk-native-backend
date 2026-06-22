package com.pk.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.pk")
public class PkAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(PkAppApplication.class, args);
    }
}
