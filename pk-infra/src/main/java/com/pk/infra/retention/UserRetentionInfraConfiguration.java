package com.pk.infra.retention;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(UserRetentionProperties.class)
public class UserRetentionInfraConfiguration {
    @Bean
    UserRetentionJdbcRepository userRetentionJdbcRepository(JdbcTemplate jdbcTemplate) {
        return new UserRetentionJdbcRepository(jdbcTemplate);
    }

    @Bean
    UserRetentionMarkService userRetentionMarkService(
            UserRetentionJdbcRepository userRetentionJdbcRepository,
            UserRetentionProperties userRetentionProperties,
            PlatformTransactionManager transactionManager
    ) {
        return new UserRetentionMarkService(
                userRetentionJdbcRepository,
                userRetentionProperties,
                transactionManager
        );
    }

    @Bean
    UserRetentionPurgeService userRetentionPurgeService(
            UserRetentionJdbcRepository userRetentionJdbcRepository,
            UserRetentionProperties userRetentionProperties,
            PlatformTransactionManager transactionManager
    ) {
        return new UserRetentionPurgeService(
                userRetentionJdbcRepository,
                userRetentionProperties,
                transactionManager
        );
    }
}
