package com.pk.infra.agreement;

import com.pk.core.agreement.port.UserAgreementRecordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgreementInfraConfiguration {
    @Bean
    AgreementFacade agreementFacade(UserAgreementRecordRepository userAgreementRecordRepository) {
        return new AgreementFacade(userAgreementRecordRepository);
    }
}
