package com.pk.infra.accountclosure;

import com.pk.core.auth.port.LenderUserDisablePort;
import com.pk.infra.accountclosure.mapper.AccountClosureMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountClosureInfraConfiguration {
    @Bean
    AccountClosureFacade accountClosureFacade(
            AccountClosureMapper accountClosureMapper,
            LenderUserDisablePort lenderUserDisablePort,
            AccountClosureLocalWriter localWriter
    ) {
        return new AccountClosureFacade(
                accountClosureMapper,
                lenderUserDisablePort,
                localWriter
        );
    }
}
