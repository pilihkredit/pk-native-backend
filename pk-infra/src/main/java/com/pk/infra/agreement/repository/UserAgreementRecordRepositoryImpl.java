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
        param.setMobileNo(data.mobileNo());
        param.setPartnerUserId(data.partnerUserId());
        param.setDeviceNo(data.deviceNo());
        param.setProfileId(data.profileId());
        param.setAgreementType(data.agreementType());
        param.setAgreed(data.agreed());
        param.setAgreedAt(data.agreedAt());
        userAgreementRecordMapper.insert(param);
        return new UserAgreementRecordData(
                param.getId(),
                data.mobileNo(),
                data.partnerUserId(),
                data.deviceNo(),
                data.profileId(),
                data.agreementType(),
                data.agreed(),
                data.agreedAt()
        );
    }

    @Override
    public List<UserAgreementRecordData> findLatestByMobileNo(String mobileNo, List<String> agreementTypes) {
        return userAgreementRecordMapper.findLatestByMobileNo(mobileNo, agreementTypes);
    }
}
