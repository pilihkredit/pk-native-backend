package com.pk.core.profile.port;

import com.pk.core.profile.ProfileBankCardData;
import java.util.Optional;

public interface ProfileBankCardRepository {
    Optional<ProfileBankCardData> findDefaultByProfileId(long profileId);

    Optional<ProfileBankCardData> findByLastRequestId(String lastRequestId);

    Optional<ProfileBankCardData> findByCardNoHash(String cardNoHash);

    Optional<ProfileBankCardData> findActiveByProfileIdAndCardNoHash(long profileId, String cardNoHash);

    int countActiveByProfileId(long profileId);

    void insert(ProfileBankCardData data);

    void updateById(ProfileBankCardData data);

    void clearDefaultByProfileId(long profileId);

    void softDeleteById(long id, String lastRequestId);

    void updateLastLenderAudit(String lastRequestId, String requestDataJson, String responseDataJson);
}
