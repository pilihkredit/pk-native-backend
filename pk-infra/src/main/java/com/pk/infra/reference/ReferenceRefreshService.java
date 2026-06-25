package com.pk.infra.reference;

import com.pk.core.reference.port.RefAreaRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReferenceRefreshService {
    private static final Logger log = LoggerFactory.getLogger(ReferenceRefreshService.class);

    private final BankReferenceFacade bankReferenceFacade;
    private final AreaReferenceFacade areaReferenceFacade;
    private final RefAreaRepository refAreaRepository;

    public ReferenceRefreshService(
            BankReferenceFacade bankReferenceFacade,
            AreaReferenceFacade areaReferenceFacade,
            RefAreaRepository refAreaRepository
    ) {
        this.bankReferenceFacade = bankReferenceFacade;
        this.areaReferenceFacade = areaReferenceFacade;
        this.refAreaRepository = refAreaRepository;
    }

    public void refreshBanks() {
        log.info("Refreshing ref_bank from lender");
        bankReferenceFacade.refreshFromLender();
    }

    public void refreshAreas() {
        Set<String> parentCodes = new LinkedHashSet<>();
        parentCodes.add("");
        parentCodes.addAll(refAreaRepository.findRefreshableParentCodes());
        for (String parentCode : parentCodes) {
            log.info("Refreshing ref_area children for parentCode={}", parentCode.isEmpty() ? "<root>" : parentCode);
            try {
                areaReferenceFacade.refreshFromLender(parentCode);
            } catch (RuntimeException exception) {
                log.warn("Area refresh failed for parentCode={}: {}", parentCode, exception.getMessage());
            }
        }
    }
}
