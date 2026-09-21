package com.pk.core.auth.port;

/** Notifies the lender that a partner user account should be disabled (account closure). */
public interface LenderUserDisablePort {
    void disableUser(String partnerUserId);
}
