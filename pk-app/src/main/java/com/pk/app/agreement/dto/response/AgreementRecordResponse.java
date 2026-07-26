package com.pk.app.agreement.dto.response;

import com.pk.core.agreement.UserAgreementRecordData;
import java.time.Instant;
import java.util.List;

public record AgreementRecordResponse(
        long recordId,
        String mobileNo,
        String partnerUserId,
        String deviceNo,
        Long userId,
        String agreementType,
        Boolean agreed,
        Instant agreedAt
) {
    public static AgreementRecordResponse from(UserAgreementRecordData data) {
        return new AgreementRecordResponse(
                data.id(),
                data.mobileNo(),
                data.partnerUserId(),
                data.deviceNo(),
                data.userId(),
                data.agreementType(),
                data.agreed(),
                data.agreedAt()
        );
    }

    public static List<AgreementRecordResponse> fromList(List<UserAgreementRecordData> records) {
        return records.stream().map(AgreementRecordResponse::from).toList();
    }
}
