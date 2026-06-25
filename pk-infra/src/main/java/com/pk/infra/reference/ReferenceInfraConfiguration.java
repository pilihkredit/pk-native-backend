package com.pk.infra.reference;

import com.pk.core.profile.port.AreaHierarchyValidator;
import com.pk.core.reference.port.LenderAreaPort;
import com.pk.core.reference.port.LenderBankPort;
import com.pk.core.reference.port.RefAreaRepository;
import com.pk.core.reference.port.RefBankRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReferenceProperties.class)
public class ReferenceInfraConfiguration {
    @Bean
    BankReferenceFacade bankReferenceFacade(
            RefBankRepository refBankRepository,
            LenderBankPort lenderBankPort,
            ReferenceProperties referenceProperties
    ) {
        return new BankReferenceFacade(refBankRepository, lenderBankPort, referenceProperties);
    }

    @Bean
    AreaReferenceFacade areaReferenceFacade(
            RefAreaRepository refAreaRepository,
            LenderAreaPort lenderAreaPort,
            ReferenceProperties referenceProperties
    ) {
        return new AreaReferenceFacade(refAreaRepository, lenderAreaPort, referenceProperties);
    }

    @Bean
    AreaHierarchyValidator areaHierarchyValidator(AreaReferenceFacade areaReferenceFacade) {
        return new RefAreaHierarchyValidator(areaReferenceFacade);
    }
}
