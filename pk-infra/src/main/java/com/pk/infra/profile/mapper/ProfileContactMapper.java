package com.pk.infra.profile.mapper;

import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfileContactData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileContactMapper {
    ProfileContactsModuleData findModuleByUserId(@Param("userId") long userId);

    List<ProfileContactData> findContactsByUserId(@Param("userId") long userId);

    int upsertModule(
            @Param("userId") long userId,
            @Param("moduleStatus") String moduleStatus,
            @Param("lastRequestId") String lastRequestId
    );

    int deleteContactsByUserId(@Param("userId") long userId);

    int insertContact(
            @Param("userId") long userId,
            @Param("sortNo") int sortNo,
            @Param("relationship") int relationship,
            @Param("contactName") String contactName,
            @Param("contactMobile") String contactMobile
    );

    int updateLastLenderInteraction(@Param("userId") long userId, @Param("externalInteractionId") Long externalInteractionId);
}
