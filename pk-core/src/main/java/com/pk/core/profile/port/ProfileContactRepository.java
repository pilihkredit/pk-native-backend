package com.pk.core.profile.port;

import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileContactsModuleData;
import java.util.Optional;

public interface ProfileContactRepository {
    Optional<ProfileContactsModuleData> findModuleByUserId(long userId);

    java.util.List<ProfileContactData> findContactsByUserId(long userId);

    void replaceContacts(long userId, ProfileContactsModuleData module, java.util.List<ProfileContactData> contacts);

    void updateLastLenderInteraction(long userId, Long externalInteractionId);
}
