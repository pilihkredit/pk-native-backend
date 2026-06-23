package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileServiceFacadeTest {
    private ProfilePersonalRepository profilePersonalRepository;
    private ProfileServiceFacade facade;

    @BeforeEach
    void setUp() {
        profilePersonalRepository = mock(ProfilePersonalRepository.class);
        SensitiveFieldEncryptor encryptor = plaintext -> new EncryptedField("cipher", new byte[12], new byte[16]);
        facade = new ProfileServiceFacade(
                profilePersonalRepository,
                new BasicAreaHierarchyValidator(),
                encryptor,
                new ProfileEnumValidator(new PendanaanProfileEnumCatalog())
        );
        when(profilePersonalRepository.findByProfileId(10L)).thenReturn(Optional.empty());
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
