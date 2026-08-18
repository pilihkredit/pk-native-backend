package com.pk.worker;

import com.pk.adapter.apipartner.ApiPartnerAdapterConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.pk",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ApiPartnerAdapterConfiguration.class
        )
)
@EnableScheduling
public class PkWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PkWorkerApplication.class, args);
    }
}
