package com.pk.adapter.apipartner;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@AutoConfigureAfter(name = "com.pk.infra.provider.LenderProviderDatabaseAutoConfiguration")
@Import(ApiPartnerAdapterConfiguration.class)
public class ApiPartnerAdapterAutoConfiguration {
}
