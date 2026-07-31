package com.pk.app;

import com.pk.adapter.pendanaan.PendanaanAdapterConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.pk",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = PendanaanAdapterConfiguration.class
        )
)
public class PkAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(PkAppApplication.class, args);
    }
}
