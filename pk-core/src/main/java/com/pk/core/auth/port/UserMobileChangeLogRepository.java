package com.pk.core.auth.port;

public interface UserMobileChangeLogRepository {
    void insert(UserMobileChangeLogEntry entry);

    record UserMobileChangeLogEntry(
            long userId,
            String partnerUserId,
            String oldMobileNo,
            String newMobileNo,
            String status,
            String operatorType,
            String faceTicketId,
            String otpTicketId
    ) {
    }
}
