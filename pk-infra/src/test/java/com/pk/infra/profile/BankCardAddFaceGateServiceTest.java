package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.BankCardAddFaceVerificationRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankCardAddFaceGateServiceTest {
    private BankCardAddFaceVerificationRepository repository;
    private BankCardAddFaceGateService gate;

    @BeforeEach
    void setUp() {
        repository = mock(BankCardAddFaceVerificationRepository.class);
        gate = new BankCardAddFaceGateService(repository);
    }

    @Test
    void consumeBeforeSaveRejectsMissingToken() {
        assertThatThrownBy(() -> gate.consumeBeforeSave(1L, "device-1", null))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
    }

    @Test
    void consumeBeforeSaveRejectsExpiredOrMismatchedTicket() {
        Instant future = Instant.now().plusSeconds(300);
        Instant past = Instant.now().minusSeconds(30);
        when(repository.findByToken("ok")).thenReturn(Optional.of(ticket(1L, "device-1", "ok", future)));
        when(repository.findByToken("wrong-user"))
                .thenReturn(Optional.of(ticket(2L, "device-1", "wrong-user", future)));
        when(repository.findByToken("wrong-device"))
                .thenReturn(Optional.of(ticket(1L, "device-2", "wrong-device", future)));
        when(repository.findByToken("expired"))
                .thenReturn(Optional.of(ticket(1L, "device-1", "expired", past)));

        assertThatThrownBy(() -> gate.consumeBeforeSave(1L, "device-1", "wrong-user"))
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
        assertThatThrownBy(() -> gate.consumeBeforeSave(1L, "device-1", "wrong-device"))
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
        assertThatThrownBy(() -> gate.consumeBeforeSave(1L, "device-1", "expired"))
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
    }

    @Test
    void consumeBeforeSaveRejectsWhenConsumeReturnsFalse() {
        Instant future = Instant.now().plusSeconds(300);
        when(repository.findByToken("tok-1"))
                .thenReturn(Optional.of(ticket(1L, "device-1", "tok-1", future)));
        when(repository.consume(eq("tok-1"), any(Instant.class))).thenReturn(false);

        assertThatThrownBy(() -> gate.consumeBeforeSave(1L, "  device-1  ", "tok-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
    }

    @Test
    void consumeBeforeSaveConsumesValidTicket() {
        Instant future = Instant.now().plusSeconds(300);
        when(repository.findByToken("tok-1"))
                .thenReturn(Optional.of(ticket(1L, "device-1", "tok-1", future)));
        when(repository.consume(eq("tok-1"), any(Instant.class))).thenReturn(true);

        gate.consumeBeforeSave(1L, "device-1", "tok-1");

        verify(repository).consume(eq("tok-1"), any(Instant.class));
    }

    private static BankCardAddFaceVerificationRepository.FaceVerificationData ticket(
            long userId,
            String deviceNo,
            String token,
            Instant expiresAt
    ) {
        return new BankCardAddFaceVerificationRepository.FaceVerificationData(
                1L,
                token,
                userId,
                "U1",
                "req-1",
                deviceNo,
                "live-1",
                "IDENTITY",
                88D,
                "VERIFIED_PENDING",
                expiresAt,
                null
        );
    }
}
