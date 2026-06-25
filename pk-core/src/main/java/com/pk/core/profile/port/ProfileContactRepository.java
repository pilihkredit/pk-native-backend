package com.pk.core.profile.port;

import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileContactsModuleData;
import java.util.Optional;

public interface ProfileContactRepository {
    Optional<ProfileContactsModuleData> findModuleByProfileId(long profileId);

    java.util.List<ProfileContactData> findContactsByProfileId(long profileId);

    void replaceContacts(long profileId, ProfileContactsModuleData module, java.util.List<ProfileContactData> contacts);
}
