package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileBankCardRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileBankCardMapper {
    ProfileBankCardRow findDefaultByProfileId(@Param("profileId") long profileId);

    ProfileBankCardRow findByLastRequestId(@Param("lastRequestId") String lastRequestId);

    ProfileBankCardRow findByCardNoHash(@Param("cardNoHash") String cardNoHash);

    ProfileBankCardRow findActiveByProfileIdAndCardNoHash(
            @Param("profileId") long profileId,
            @Param("cardNoHash") String cardNoHash
    );

    int countActiveByProfileId(@Param("profileId") long profileId);

    int insert(ProfileBankCardRow row);

    int updateById(ProfileBankCardRow row);

    int clearDefaultByProfileId(@Param("profileId") long profileId);

    int softDeleteById(@Param("id") long id, @Param("lastRequestId") String lastRequestId);

    int updateLastLenderInteraction(@Param("lastRequestId") String lastRequestId, @Param("externalInteractionId") Long externalInteractionId);
}
