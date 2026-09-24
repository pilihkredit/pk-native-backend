package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.FaceComparisonBaseline;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.infra.profile.mapper.FaceBaselineMapper;
import com.pk.infra.profile.repository.FaceBaselineCandidateRow;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FaceComparisonBaselineResolverTest {
    private FaceBaselineMapper faceBaselineMapper;
    private ProfileIdentityRepository profileIdentityRepository;
    private FaceComparisonBaselineResolver resolver;

    @BeforeEach
    void setUp() {
        faceBaselineMapper = mock(FaceBaselineMapper.class);
        profileIdentityRepository = mock(ProfileIdentityRepository.class);
        resolver = new FaceComparisonBaselineResolver(faceBaselineMapper, profileIdentityRepository);
    }

    @Test
    void prefersLatestSuccessfulCompareOverIdentity() {
        Instant comparedAt = Instant.parse("2026-01-02T00:00:00Z");
        when(faceBaselineMapper.findLatestSuccessfulCompareByUserId(1L))
                .thenReturn(new FaceBaselineCandidateRow("candidate-ref", "DEVICE_SWITCH_LOGIN", comparedAt));

        Optional<FaceComparisonBaseline> baseline = resolver.resolve(1L);

        assertThat(baseline).isPresent();
        assertThat(baseline.get().baselineType()).isEqualTo(FaceComparisonBaseline.TYPE_DEVICE_SWITCH_LOGIN);
        assertThat(baseline.get().faceEncryptedRef()).isEqualTo("candidate-ref");
        assertThat(baseline.get().comparedAt()).isEqualTo(comparedAt);
    }

    @Test
    void fallsBackToIdentityFaceWhenNoAuditRow() {
        when(faceBaselineMapper.findLatestSuccessfulCompareByUserId(1L)).thenReturn(null);
        when(profileIdentityRepository.findByUserId(1L)).thenReturn(Optional.of(identityWithFace("kyc-face-ref")));

        Optional<FaceComparisonBaseline> baseline = resolver.resolve(1L);

        assertThat(baseline).isPresent();
        assertThat(baseline.get().baselineType()).isEqualTo(FaceComparisonBaseline.TYPE_IDENTITY);
        assertThat(baseline.get().faceEncryptedRef()).isEqualTo("kyc-face-ref");
    }

    @Test
    void resolveOrThrowWhenNoBaselineAnywhere() {
        when(faceBaselineMapper.findLatestSuccessfulCompareByUserId(1L)).thenReturn(null);
        when(profileIdentityRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolveOrThrow(1L))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.FACE_RECOGNITION_FAILED);

        assertThat(resolver.hasComparableBaseline(1L)).isFalse();
    }

    @Test
    void hasComparableBaselineTrueWhenIdentityFaceExists() {
        when(faceBaselineMapper.findLatestSuccessfulCompareByUserId(1L)).thenReturn(null);
        when(profileIdentityRepository.findByUserId(1L)).thenReturn(Optional.of(identityWithFace("kyc-face-ref")));

        assertThat(resolver.hasComparableBaseline(1L)).isTrue();
    }

    private static ProfileIdentityData identityWithFace(String faceRef) {
        return new ProfileIdentityData(
                1L,
                "Name",
                new EncryptedField("id", new byte[0], new byte[0]),
                "hash",
                "COMPLETED",
                "req-1",
                null,
                null,
                null,
                faceRef,
                null,
                null,
                null,
                null,
                null
        );
    }
}
