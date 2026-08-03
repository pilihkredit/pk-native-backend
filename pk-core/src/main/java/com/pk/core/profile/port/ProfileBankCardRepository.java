package com.pk.core.profile.port;

import com.pk.core.profile.ProfileBankCardData;
import java.util.Optional;

public interface ProfileBankCardRepository {
    Optional<ProfileBankCardData> findDefaultByUserId(long userId);

    Optional<ProfileBankCardData> findByLastRequestId(String lastRequestId);

    Optional<ProfileBankCardData> findByCardNoHash(String cardNoHash);

    Optional<ProfileBankCardData> findActiveByUserIdAndCardNoHash(long userId, String cardNoHash);

    int countActiveByUserId(long userId);

    void insert(ProfileBankCardData data);

    void updateById(ProfileBankCardData data);

    void clearDefaultByUserId(long userId);

    void setDefaultByUserIdAndCardId(long userId, long cardId);

    void softDeleteById(long id, String lastRequestId);

    void updateLastLenderInteraction(String lastRequestId, Long externalInteractionId);
}
