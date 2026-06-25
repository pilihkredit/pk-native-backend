package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.ProfileWorkData;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.ProfileWorkRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.List;

public class ProfileSyncPayloadLoader {
    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileWorkRepository profileWorkRepository;
    private final ProfileContactRepository profileContactRepository;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;

    public ProfileSyncPayloadLoader(
            ProfilePersonalRepository profilePersonalRepository,
            ProfileWorkRepository profileWorkRepository,
            ProfileContactRepository profileContactRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileWorkRepository = profileWorkRepository;
        this.profileContactRepository = profileContactRepository;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
    }

    public ProfileSyncPayload load(long profileId, ProfileSyncModule module) {
        return switch (module) {
            case PERSONAL -> loadPersonal(profileId);
            case WORK -> loadWork(profileId);
            case CONTACT -> loadContacts(profileId);
            case BANK_CARD -> throw new IllegalStateException("Bank card payload must be supplied explicitly");
        };
    }

    private ProfileSyncPayload.PersonalProfilePayload loadPersonal(long profileId) {
        ProfilePersonalData data = profilePersonalRepository.findByProfileId(profileId)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));
        String motherSurname = sensitiveFieldEncryptor.decrypt(data.motherSurname());
        return new ProfileSyncPayload.PersonalProfilePayload(
                data.provinceCode(),
                data.cityCode(),
                data.districtCode(),
                data.address(),
                data.educationDegree(),
                motherSurname
        );
    }

    private ProfileSyncPayload.WorkProfilePayload loadWork(long profileId) {
        ProfileWorkData data = profileWorkRepository.findByProfileId(profileId)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));
        return new ProfileSyncPayload.WorkProfilePayload(
                data.industry(),
                data.companyName(),
                data.workProvinceCode(),
                data.workCityCode(),
                data.workDistrictCode(),
                data.workAddress(),
                data.income(),
                data.payday(),
                data.professionDegree()
        );
    }

    private ProfileSyncPayload.ContactProfilePayload loadContacts(long profileId) {
        List<ProfileContactData> contacts = profileContactRepository.findContactsByProfileId(profileId);
        if (contacts.isEmpty()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return new ProfileSyncPayload.ContactProfilePayload(
                contacts.stream()
                        .map(contact -> new ProfileSyncPayload.ContactProfilePayload.ContactItem(
                                contact.relationship(),
                                contact.contactName(),
                                contact.contactMobile()
                        ))
                        .toList()
        );
    }

    public static void validateDevice(LenderDeviceContext device) {
        if (device == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (isBlank(device.deviceNo())) {
            throw new ApiException(ApiCode.DEVICE_NO_REQUIRED);
        }
        if (isBlank(device.appVersion()) || isBlank(device.packageName()) || isBlank(device.systemPlatform())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (isBlank(device.appName())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        String platform = device.systemPlatform().trim().toLowerCase();
        if (!"ios".equals(platform) && !"android".equals(platform)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
