package com.pk.infra.profile.repository;

import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.infra.profile.mapper.ProfileContactMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileContactRepositoryImpl implements ProfileContactRepository {
    private final ProfileContactMapper profileContactMapper;

    public ProfileContactRepositoryImpl(ProfileContactMapper profileContactMapper) {
        this.profileContactMapper = profileContactMapper;
    }

    @Override
    public Optional<ProfileContactsModuleData> findModuleByProfileId(long profileId) {
        return Optional.ofNullable(profileContactMapper.findModuleByProfileId(profileId));
    }

    @Override
    public List<ProfileContactData> findContactsByProfileId(long profileId) {
        return profileContactMapper.findContactsByProfileId(profileId);
    }

    @Override
    public void replaceContacts(
            long profileId,
            ProfileContactsModuleData module,
            List<ProfileContactData> contacts
    ) {
        profileContactMapper.upsertModule(
                profileId,
                module.mobileNo(),
                module.moduleStatus(),
                module.lastRequestId()
        );
        profileContactMapper.deleteContactsByProfileId(profileId);
        for (ProfileContactData contact : contacts) {
            profileContactMapper.insertContact(
                    profileId,
                    contact.mobileNo(),
                    contact.sortNo(),
                    contact.relationship(),
                    contact.contactName(),
                    contact.contactMobile()
            );
        }
    }

    @Override
    public void updateLastLenderInteraction(long profileId, Long externalInteractionId) {
        profileContactMapper.updateLastLenderInteraction(profileId, externalInteractionId);
    }
}
