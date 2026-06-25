package com.pk.core.profile.port;

import com.pk.core.profile.ProfileBankCardData;
import java.util.Optional;

public interface ProfileBankCardRepository {
    Optional<ProfileBankCardData> findByProfileId(long profileId);

    Optional<ProfileBankCardData> findByCardNoHash(String cardNoHash);

    void upsert(ProfileBankCardData data);
}
