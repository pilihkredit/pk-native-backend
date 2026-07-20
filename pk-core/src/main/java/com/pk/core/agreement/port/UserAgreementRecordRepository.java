package com.pk.core.agreement.port;

import com.pk.core.agreement.UserAgreementRecordData;
import java.time.Instant;
import java.util.List;

public interface UserAgreementRecordRepository {
    UserAgreementRecordData insert(UserAgreementRecordInsert data);

    List<UserAgreementRecordData> findLatestByMobileNo(String mobileNo, List<String> agreementTypes);

    record UserAgreementRecordInsert(
            String mobileNo,
            String partnerUserId,
            String deviceNo,
            Long profileId,
            String agreementType,
            Boolean agreed,
            Instant agreedAt
    ) {
    }
}
