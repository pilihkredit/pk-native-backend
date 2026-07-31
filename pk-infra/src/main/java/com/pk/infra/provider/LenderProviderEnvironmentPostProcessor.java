package com.pk.infra.provider;

import java.util.Map;
import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class LenderProviderEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private final DeferredLog log = new DeferredLog();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean loaded = LenderProviderPropertySourceContributor.contribute(environment);
        if (loaded) {
            log.info("Lender provider database property source applied during environment post-processing");
        }
        log.replayTo(LenderProviderEnvironmentPostProcessor.class);
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }
}
