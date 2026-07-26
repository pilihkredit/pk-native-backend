package com.pk.core.agreement.port;

import com.pk.core.agreement.UserAgreementRecordData;
import java.time.Instant;
import java.util.List;

public interface UserAgreementRecordRepository {
    UserAgreementRecordData insert(UserAgreementRecordInsert data);

    List<UserAgreementRecordData> findLatestByMobileNo(String mobileNo, List<String> agreementTypes);

    record UserAgreementRecordInsert(
            String partnerUserId,
            String deviceNo,
            Long userId,
            String agreementType,
            Boolean agreed,
            Instant agreedAt,
            Long clickedAtMs
    ) {
    }
}
