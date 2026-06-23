package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileServiceFacadeTest {
    private ProfilePersonalRepository profilePersonalRepository;
    private ProfileContactRepository profileContactRepository;
    private ProfileServiceFacade facade;

    @BeforeEach
    void setUp() {
        profilePersonalRepository = mock(ProfilePersonalRepository.class);
        profileContactRepository = mock(ProfileContactRepository.class);
        SensitiveFieldEncryptor encryptor = plaintext -> new EncryptedField("cipher", new byte[12], new byte[16]);
        facade = new ProfileServiceFacade(
                profilePersonalRepository,
                profileContactRepository,
                new BasicAreaHierarchyValidator(),
                encryptor,
                new ProfileEnumValidator(new PendanaanProfileEnumCatalog())
        );
        when(profilePersonalRepository.findByProfileId(10L)).thenReturn(Optional.empty());
        when(profileContactRepository.findModuleByProfileId(10L)).thenReturn(Optional.empty());
    }

    @Test
    void savesPersonalModuleAndReturnsCompleted() {
        var result = facade.savePersonal(10L, sampleCommand("req-1"));

        assertThat(result.requestId()).isEqualTo("req-1");
        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        verify(profilePersonalRepository).upsert(any());
    }

    @Test
    void returnsCompletedWithoutRewriteForSameRequestId() {
        when(profilePersonalRepository.findByProfileId(10L)).thenReturn(Optional.of(
                new ProfilePersonalData(
                        10L,
                        "31",
                        "3171",
                        "317101",
                        "Jl. Example 1",
                        5,
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        "user@example.com",
                        "COMPLETED",
                        "req-1"
                )
        ));

        var result = facade.savePersonal(10L, sampleCommand("req-1"));

        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        verify(profilePersonalRepository, never()).upsert(any());
    }

    @Test
    void rejectsInvalidEducationDegree() {
        var command = new ProfileServiceFacade.PersonalSaveCommand(
                "req-2",
                "31",
                "3171",
                "317101",
                "Jl. Example 1",
                9,
                "Siti",
                null
        );

        assertThatThrownBy(() -> facade.savePersonal(10L, command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_EDUCATION_DEGREE);
    }

    @Test
    void savesContactsModuleAndReturnsCompleted() {
        var result = facade.saveContacts(10L, "81234567890", sampleContactsCommand("req-contact-1"));

        assertThat(result.requestId()).isEqualTo("req-contact-1");
        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        verify(profileContactRepository).replaceContacts(anyLong(), any(), any());
    }

    @Test
    void returnsCompletedWithoutRewriteForSameContactsRequestId() {
        when(profileContactRepository.findModuleByProfileId(10L)).thenReturn(Optional.of(
                new ProfileContactsModuleData(10L, "COMPLETED", "req-contact-1")
        ));

        var result = facade.saveContacts(10L, "81234567890", sampleContactsCommand("req-contact-1"));

        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        verify(profileContactRepository, never()).replaceContacts(anyLong(), any(), any());
    }

    @Test
    void rejectsContactsBelowMinimumCount() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-2",
                List.of(new ProfileServiceFacade.ContactItemCommand(0, "SITI", "81234567801"))
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void rejectsInvalidContactRelationship() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-3",
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(99, "SITI", "81234567801"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567802")
                )
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_CONTACT_RELATIONSHIP);
    }

    @Test
    void rejectsContactMobileSameAsOwnNumber() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-4",
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(0, "SITI", "81234567890"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567802")
                )
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.CONTACT_MOBILE_SAME_AS_OWN);
    }

    @Test
    void rejectsDuplicateContactMobiles() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-5",
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(0, "SITI", "81234567801"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567801")
                )
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void rejectsInvalidContactMobileFormat() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-6",
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(0, "SITI", "081234567801"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567802")
                )
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_MOBILE_NUMBER);
    }

    private static ProfileServiceFacade.ContactsSaveCommand sampleContactsCommand(String requestId) {
        return new ProfileServiceFacade.ContactsSaveCommand(
                requestId,
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(0, "SITI", "81234567801"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567802")
                )
        );
    }

    private static ProfileServiceFacade.PersonalSaveCommand sampleCommand(String requestId) {
        return new ProfileServiceFacade.PersonalSaveCommand(
                requestId,
                "31",
                "3171",
                "317101",
                "Jl. Example 1",
                5,
                "Siti",
                "user@example.com"
        );
    }
}
