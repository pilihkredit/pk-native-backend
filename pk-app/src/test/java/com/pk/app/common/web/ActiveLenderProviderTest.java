package com.pk.app.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActiveLenderProviderTest {
    @Test
    void enrichFillsBlankProviderFields() {
        ActiveLenderProvider provider = new ActiveLenderProvider("pendanaan", "Pendanaan Test");
        ApiResponse<String> enriched = provider.enrich(ApiResponse.success("ok", "t1"));

        assertThat(enriched.lenderProvider()).isEqualTo("pendanaan");
        assertThat(enriched.lenderProviderName()).isEqualTo("Pendanaan Test");
        assertThat(enriched.data()).isEqualTo("ok");
    }

    @Test
    void enrichKeepsExistingProviderFields() {
        ActiveLenderProvider provider = new ActiveLenderProvider("pendanaan", "Pendanaan Test");
        ApiResponse<String> source = ApiResponse.success("ok", "t1")
                .withLenderProvider("other", "Other Name");

        ApiResponse<String> enriched = provider.enrich(source);

        assertThat(enriched.lenderProvider()).isEqualTo("other");
        assertThat(enriched.lenderProviderName()).isEqualTo("Other Name");
    }

    @Test
    void treatsBlankProviderNameAsNull() {
        ActiveLenderProvider provider = new ActiveLenderProvider("pendanaan", "  ");
        ApiResponse<Void> enriched = provider.enrich(ApiResponse.success(null, "t1"));

        assertThat(enriched.lenderProvider()).isEqualTo("pendanaan");
        assertThat(enriched.lenderProviderName()).isNull();
    }
}
