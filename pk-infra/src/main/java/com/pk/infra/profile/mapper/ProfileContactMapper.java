package com.pk.infra.profile.mapper;

import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfileContactData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileContactMapper {
    ProfileContactsModuleData findModuleByProfileId(@Param("profileId") long profileId);

    List<ProfileContactData> findContactsByProfileId(@Param("profileId") long profileId);

    int upsertModule(
            @Param("profileId") long profileId,
            @Param("mobileNo") String mobileNo,
            @Param("moduleStatus") String moduleStatus,
            @Param("lastRequestId") String lastRequestId
    );

    int deleteContactsByProfileId(@Param("profileId") long profileId);

    int insertContact(
            @Param("profileId") long profileId,
            @Param("mobileNo") String mobileNo,
            @Param("sortNo") int sortNo,
            @Param("relationship") int relationship,
            @Param("contactName") String contactName,
            @Param("contactMobile") String contactMobile
    );

    int updateLastLenderAudit(
            @Param("profileId") long profileId,
            @Param("requestDataJson") String requestDataJson,
            @Param("responseDataJson") String responseDataJson
    );
}
