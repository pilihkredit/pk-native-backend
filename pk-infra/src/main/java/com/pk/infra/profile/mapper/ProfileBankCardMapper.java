package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.ProfileBankCardRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileBankCardMapper {
    ProfileBankCardRow findDefaultByUserId(@Param("userId") long userId);

    ProfileBankCardRow findByLastRequestId(@Param("lastRequestId") String lastRequestId);

    ProfileBankCardRow findByCardNoHash(@Param("cardNoHash") String cardNoHash);

    ProfileBankCardRow findActiveByUserIdAndCardNoHash(
            @Param("userId") long userId,
            @Param("cardNoHash") String cardNoHash
    );

    int countActiveByUserId(@Param("userId") long userId);

    int insert(ProfileBankCardRow row);

    int updateById(ProfileBankCardRow row);

    int clearDefaultByUserId(@Param("userId") long userId);

    int softDeleteById(@Param("id") long id, @Param("lastRequestId") String lastRequestId);

    int updateLastLenderInteraction(@Param("lastRequestId") String lastRequestId, @Param("externalInteractionId") Long externalInteractionId);
}
