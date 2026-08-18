package com.pk.app;

import com.pk.adapter.apipartner.ApiPartnerAdapterConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.pk",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ApiPartnerAdapterConfiguration.class
        )
)
public class PkAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(PkAppApplication.class, args);
    }
}
