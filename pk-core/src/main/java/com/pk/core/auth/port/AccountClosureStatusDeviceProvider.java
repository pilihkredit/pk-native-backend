package com.pk.core.auth.port;

import com.pk.core.profile.sync.LenderDeviceContext;

/** Supplies minimal {@code openUserDevice} for account-closure lender status queries. */
public interface AccountClosureStatusDeviceProvider {
    LenderDeviceContext create();
}
