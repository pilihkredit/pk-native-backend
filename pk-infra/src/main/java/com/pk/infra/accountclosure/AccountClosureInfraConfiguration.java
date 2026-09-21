package com.pk.infra.accountclosure;

import com.pk.core.auth.port.LenderUserDisablePort;
import com.pk.infra.accountclosure.mapper.AccountClosureMapper;
import com.pk.infra.auth.mapper.UserAuthMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountClosureInfraConfiguration {
    @Bean
    AccountClosureFacade accountClosureFacade(
            UserAuthMapper userAuthMapper,
            AccountClosureMapper accountClosureMapper,
            LenderUserDisablePort lenderUserDisablePort
    ) {
        return new AccountClosureFacade(
                userAuthMapper,
                accountClosureMapper,
                lenderUserDisablePort
        );
    }
}
