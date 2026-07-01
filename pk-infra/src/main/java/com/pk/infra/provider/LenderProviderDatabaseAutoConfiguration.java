package com.pk.infra.provider;

import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(BeanDefinitionRegistryPostProcessor.class)
public class LenderProviderDatabaseAutoConfiguration {
    @Bean
    static BeanDefinitionRegistryPostProcessor lenderProviderDatabaseRegistryPostProcessor() {
        return new LenderProviderDatabaseRegistryPostProcessor();
    }
}
