package com.pk.adapter.pendanaan;

import com.pk.core.reference.port.LenderBankPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
class PendanaanAdapterStartupLogger {
    private static final Logger log = LoggerFactory.getLogger(PendanaanAdapterStartupLogger.class);

    private final PendanaanProperties properties;
    private final LenderBankPort lenderBankPort;
    private final Environment environment;

    PendanaanAdapterStartupLogger(
            PendanaanProperties properties,
            LenderBankPort lenderBankPort,
            Environment environment
    ) {
        this.properties = properties;
        this.lenderBankPort = lenderBankPort;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    void logEffectiveAdapter() {
        String configSource = environment.getProperty("pk.lender.config.source", "db");
        log.info(
                "Pendanaan effective config: configSource={}, mode={}, httpCredentialsPresent={}, bankPort={}",
                configSource,
                properties.mode(),
                properties.httpCredentialsPresent(),
                lenderBankPort.getClass().getSimpleName()
        );
        if (!properties.httpEnabled() || !properties.httpCredentialsPresent()) {
            log.warn(
                    "Lender HTTP is inactive; /bank/list syncs from in-memory fake data and will not emit Lender request/response logs. "
                            + "Apply sql/create_pk_schema.sql (or recreate Docker MySQL volume) and restart."
            );
            return;
        }
        if (lenderBankPort instanceof FakePendanaanBankAdapter) {
            log.warn(
                    "Pendanaan properties indicate HTTP mode but FakePendanaanBankAdapter is active; "
                            + "check lender adapter bean wiring"
            );
        }
    }
}
