package com.pk.adapter.pendanaan;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@AutoConfigureAfter(name = "com.pk.infra.provider.LenderProviderDatabaseAutoConfiguration")
@Import(PendanaanAdapterConfiguration.class)
public class PendanaanAdapterAutoConfiguration {
}
