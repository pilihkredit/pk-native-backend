package com.pk.core.profile.port;

import com.pk.core.profile.ProfileBankCardData;
import java.util.Optional;

public interface ProfileBankCardRepository {
    Optional<ProfileBankCardData> findDefaultByProfileId(long profileId);

    Optional<ProfileBankCardData> findByLastRequestId(String lastRequestId);

    Optional<ProfileBankCardData> findByCardNoHash(String cardNoHash);

    void insert(ProfileBankCardData data);

    void updateById(ProfileBankCardData data);

    void clearDefaultByProfileId(long profileId);

    void updateLastLenderAudit(String lastRequestId, String requestDataJson, String responseDataJson);
}
