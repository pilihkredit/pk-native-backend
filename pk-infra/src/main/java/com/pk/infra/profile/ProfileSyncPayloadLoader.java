package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.List;

public class ProfileSyncPayloadLoader {
    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileContactRepository profileContactRepository;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;

    public ProfileSyncPayloadLoader(
            ProfilePersonalRepository profilePersonalRepository,
            ProfileContactRepository profileContactRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileContactRepository = profileContactRepository;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
    }

    public ProfileSyncPayload load(long userId, ProfileSyncModule module) {
        return switch (module) {
            case MOBILE -> throw new IllegalStateException("Mobile payload must be supplied explicitly");
            case PERSONAL -> loadPersonal(userId);
            case CONTACT -> loadContacts(userId);
            case BANK_CARD -> throw new IllegalStateException("Bank card payload must be supplied explicitly");
            case IDENTITY -> throw new IllegalStateException("Identity payload must be supplied explicitly");
            case LOGIN_LOG -> throw new IllegalStateException("Login log payload must be supplied explicitly");
            case APPSFLYER_INSTALL -> throw new IllegalStateException("AppsFlyer payload must be supplied explicitly");
            case TONGDUN_DEVICE -> throw new IllegalStateException("Tongdun payload must be supplied explicitly");
        };
    }

    private ProfileSyncPayload.PersonalProfilePayload loadPersonal(long userId) {
        ProfilePersonalData data = profilePersonalRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));
        String motherSurname = sensitiveFieldEncryptor.decrypt(data.motherSurname());
        return new ProfileSyncPayload.PersonalProfilePayload(
                data.educationDegree(),
                data.industry(),
                data.income(),
                motherSurname,
                data.userEmail()
        );
    }

    private ProfileSyncPayload.ContactProfilePayload loadContacts(long userId) {
        List<ProfileContactData> contacts = profileContactRepository.findContactsByUserId(userId);
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
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "device is required");
        }
        if (isBlank(device.deviceNo())) {
            throw new ApiException(ApiCode.DEVICE_NO_REQUIRED);
        }
        if (isBlank(device.appVersion()) || isBlank(device.packageName()) || isBlank(device.systemPlatform())) {
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "device.appVersion, device.packageName, and device.systemPlatform are required"
            );
        }
        if (isBlank(device.appName())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "device.appName is required");
        }
        String platform = device.systemPlatform().trim().toLowerCase();
        if (!"ios".equals(platform) && !"android".equals(platform)) {
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "device.systemPlatform must be ios or android"
            );
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
