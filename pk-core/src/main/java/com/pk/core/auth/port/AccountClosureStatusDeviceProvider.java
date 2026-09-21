package com.pk.core.auth.port;

import com.pk.core.profile.sync.LenderDeviceContext;

/** Supplies {@code openUserDevice} for account-closure lender status queries (Ops). */
public interface AccountClosureStatusDeviceProvider {
    /**
     * @param clientIp caller IP for lender {@code device.ip} when no {@code user_device} row exists; may be null
     */
    LenderDeviceContext create(long userId, String clientIp);
}
