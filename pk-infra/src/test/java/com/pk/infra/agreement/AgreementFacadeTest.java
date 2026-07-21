package com.pk.infra.agreement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.agreement.UserAgreementRecordData;
import com.pk.core.agreement.port.UserAgreementRecordRepository;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AgreementFacadeTest {
    private UserAgreementRecordRepository repository;
    private AgreementFacade facade;

    @BeforeEach
    void setUp() {
        repository = mock(UserAgreementRecordRepository.class);
        facade = new AgreementFacade(repository);
    }

    @Test
    void createsAppendOnlyRecordsWithClientClickedAt() {
        long clickedAtMs = Instant.parse("2026-07-20T03:00:00Z").toEpochMilli();
        Instant agreedAt = Instant.ofEpochMilli(clickedAtMs);
        when(repository.insert(any())).thenAnswer(invocation -> {
            UserAgreementRecordRepository.UserAgreementRecordInsert insert = invocation.getArgument(0);
            return new UserAgreementRecordData(
                    11L,
                    insert.mobileNo(),
                    insert.partnerUserId(),
                    insert.deviceNo(),
                    insert.profileId(),
                    insert.agreementType(),
                    insert.agreed(),
                    insert.agreedAt(),
                    insert.clickedAtMs()
            );
        });

        var result = facade.createRecords(new AgreementFacade.CreateCommand(
                "81234567890",
                null,
                "device-1",
                null,
                clickedAtMs,
                List.of(
                        new AgreementFacade.AgreementItemCommand("PRIVACY_POLICY", true),
                        new AgreementFacade.AgreementItemCommand("USER_AGREEMENT", null)
                )
        ));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).agreementType()).isEqualTo("PRIVACY_POLICY");
        assertThat(result.get(0).agreed()).isTrue();
        assertThat(result.get(1).agreed()).isNull();
        assertThat(result.get(0).agreedAt()).isEqualTo(agreedAt);
        assertThat(result.get(0).clickedAtMs()).isEqualTo(clickedAtMs);

        ArgumentCaptor<UserAgreementRecordRepository.UserAgreementRecordInsert> captor =
                ArgumentCaptor.forClass(UserAgreementRecordRepository.UserAgreementRecordInsert.class);
        verify(repository, org.mockito.Mockito.times(2)).insert(captor.capture());
        assertThat(captor.getAllValues()).allMatch(item ->
                agreedAt.equals(item.agreedAt()) && clickedAtMs == item.clickedAtMs()
        );
    }

    @Test
    void rejectsDuplicateAgreementTypesInOneBatch() {
        assertThatThrownBy(() -> facade.createRecords(new AgreementFacade.CreateCommand(
                "81234567890",
                "U10001",
                "device-1",
                10L,
                Instant.parse("2026-07-20T03:00:00Z").toEpochMilli(),
                List.of(
                        new AgreementFacade.AgreementItemCommand("PRIVACY_POLICY", true),
                        new AgreementFacade.AgreementItemCommand("PRIVACY_POLICY", false)
                )
        )))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void createsAnonymousRecordWithoutMobileNo() {
        long clickedAtMs = Instant.parse("2026-07-20T03:00:00Z").toEpochMilli();
        when(repository.insert(any())).thenAnswer(invocation -> {
            UserAgreementRecordRepository.UserAgreementRecordInsert insert = invocation.getArgument(0);
            return new UserAgreementRecordData(
                    12L,
                    insert.mobileNo(),
                    insert.partnerUserId(),
                    insert.deviceNo(),
                    insert.profileId(),
                    insert.agreementType(),
                    insert.agreed(),
                    insert.agreedAt(),
                    insert.clickedAtMs()
            );
        });

        var result = facade.createRecords(new AgreementFacade.CreateCommand(
                null,
                null,
                "device-1",
                null,
                clickedAtMs,
                List.of(new AgreementFacade.AgreementItemCommand("PRIVACY_POLICY", true))
        ));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).mobileNo()).isNull();
        assertThat(result.get(0).partnerUserId()).isNull();
        assertThat(result.get(0).deviceNo()).isEqualTo("device-1");
        assertThat(result.get(0).clickedAtMs()).isEqualTo(clickedAtMs);
    }

    @Test
    void latestQueriesByMobileNo() {
        when(repository.findLatestByMobileNo("81234567890", List.of("PRIVACY_POLICY")))
                .thenReturn(List.of(new UserAgreementRecordData(
                        1L,
                        "81234567890",
                        "U10001",
                        "device-1",
                        10L,
                        "PRIVACY_POLICY",
                        true,
                        Instant.parse("2026-07-20T03:00:00Z"),
                        Instant.parse("2026-07-20T03:00:00Z").toEpochMilli()
                )));

        var result = facade.latestByMobileNo("81234567890", List.of("PRIVACY_POLICY"));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().agreed()).isTrue();
    }
}
