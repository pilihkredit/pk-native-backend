package com.pk.infra.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Loads lender credentials into the Environment before @Configuration condition evaluation.
 * Complements {@link LenderProviderEnvironmentPostProcessor} when early bootstrap JDBC is unavailable.
 */
public class LenderProviderDatabaseRegistryPostProcessor
        implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, PriorityOrdered {
    private static final Logger log = LoggerFactory.getLogger(LenderProviderDatabaseRegistryPostProcessor.class);

    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (environment == null) {
            log.warn("Skipping lender provider database config because Environment is unavailable");
            return;
        }
        LenderProviderPropertySourceContributor.contribute(environment);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no-op
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
