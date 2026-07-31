package com.pk.app.repay.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RemovedRepayBillsOverviewEndpointTest {
    @Test
    void repayBillsOverviewControllerIsRemoved() {
        assertThatThrownBy(() -> Class.forName("com.pk.app.repay.controller.RepayBillsOverviewController"))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
