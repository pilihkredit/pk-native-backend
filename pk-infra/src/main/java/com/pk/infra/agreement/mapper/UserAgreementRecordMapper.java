package com.pk.infra.agreement.mapper;

import com.pk.core.agreement.UserAgreementRecordData;
import com.pk.infra.agreement.repository.UserAgreementRecordInsertParam;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAgreementRecordMapper {
    int insert(UserAgreementRecordInsertParam data);

    List<UserAgreementRecordData> findLatestByMobileNo(
            @Param("mobileNo") String mobileNo,
            @Param("agreementTypes") List<String> agreementTypes
    );
}
