package com.pk.infra.agreement.repository;

import com.pk.core.agreement.UserAgreementRecordData;
import com.pk.core.agreement.port.UserAgreementRecordRepository;
import com.pk.infra.agreement.mapper.UserAgreementRecordMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UserAgreementRecordRepositoryImpl implements UserAgreementRecordRepository {
    private final UserAgreementRecordMapper userAgreementRecordMapper;

    public UserAgreementRecordRepositoryImpl(UserAgreementRecordMapper userAgreementRecordMapper) {
        this.userAgreementRecordMapper = userAgreementRecordMapper;
    }

    @Override
    public UserAgreementRecordData insert(UserAgreementRecordInsert data) {
        UserAgreementRecordInsertParam param = new UserAgreementRecordInsertParam();
        param.setPartnerUserId(data.partnerUserId());
        param.setDeviceNo(data.deviceNo());
        param.setUserId(data.userId());
        param.setAgreementType(data.agreementType());
        param.setAgreed(data.agreed());
        param.setAgreedAt(data.agreedAt());
        param.setClickedAtMs(data.clickedAtMs());
        userAgreementRecordMapper.insert(param);
        return new UserAgreementRecordData(
                param.getId(),
                data.partnerUserId(),
                data.deviceNo(),
                data.userId(),
                data.agreementType(),
                data.agreed(),
                data.agreedAt(),
                data.clickedAtMs(),
                null
        );
    }

    @Override
    public List<UserAgreementRecordData> findLatestByMobileNo(String mobileNo, List<String> agreementTypes) {
        return userAgreementRecordMapper.findLatestByMobileNo(mobileNo, agreementTypes);
    }
}
