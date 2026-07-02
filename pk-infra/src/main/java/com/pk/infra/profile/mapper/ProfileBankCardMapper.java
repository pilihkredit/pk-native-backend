package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileBankCardRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileBankCardMapper {
    ProfileBankCardRow findByProfileId(@Param("profileId") long profileId);
    ProfileBankCardRow findByCardNoHash(@Param("cardNoHash") String cardNoHash);
    int upsert(ProfileBankCardRow row);
    int updateLastLenderAudit(@Param("profileId") long profileId, @Param("requestDataJson") String requestDataJson, @Param("responseDataJson") String responseDataJson);
}
