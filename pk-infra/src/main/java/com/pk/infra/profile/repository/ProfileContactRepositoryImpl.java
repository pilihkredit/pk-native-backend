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
    public Optional<ProfileContactsModuleData> findModuleByUserId(long userId) {
        return Optional.ofNullable(profileContactMapper.findModuleByUserId(userId));
    }

    @Override
    public List<ProfileContactData> findContactsByUserId(long userId) {
        return profileContactMapper.findContactsByUserId(userId);
    }

    @Override
    public void replaceContacts(
            long userId,
            ProfileContactsModuleData module,
            List<ProfileContactData> contacts
    ) {
        profileContactMapper.upsertModule(
                userId,
                module.moduleStatus(),
                module.lastRequestId()
        );
        profileContactMapper.deleteContactsByUserId(userId);
        for (ProfileContactData contact : contacts) {
            profileContactMapper.insertContact(
                    userId,
                    contact.sortNo(),
                    contact.relationship(),
                    contact.contactName(),
                    contact.contactMobile()
            );
        }
    }

    @Override
    public void updateLastLenderInteraction(long userId, Long externalInteractionId) {
        profileContactMapper.updateLastLenderInteraction(userId, externalInteractionId);
    }
}
