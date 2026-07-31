package com.pk.infra.agreement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.core.agreement.port.UserAgreementRecordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgreementInfraConfiguration {
    @Bean
    AgreementFieldConfigLoader agreementFieldConfigLoader(
            AppConfigRepository appConfigRepository,
            ObjectMapper objectMapper
    ) {
        return new AgreementFieldConfigLoader(appConfigRepository, objectMapper);
    }

    @Bean
    AgreementFacade agreementFacade(UserAgreementRecordRepository userAgreementRecordRepository) {
        return new AgreementFacade(userAgreementRecordRepository);
    }
}
