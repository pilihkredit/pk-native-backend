package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.FaceComparisonBaseline;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.infra.profile.mapper.FaceBaselineMapper;
import com.pk.infra.profile.repository.FaceBaselineCandidateRow;
import java.util.Optional;

/**
 * Resolves 1:1 face baseline: prefer the candidate from the latest successful compare across
 * mobile-change, device-switch login, and bank-card-add flows; otherwise KYC identity face.
 */
public class FaceComparisonBaselineResolver {
    private final FaceBaselineMapper faceBaselineMapper;
    private final ProfileIdentityRepository profileIdentityRepository;

    public FaceComparisonBaselineResolver(
            FaceBaselineMapper faceBaselineMapper,
            ProfileIdentityRepository profileIdentityRepository
    ) {
        this.faceBaselineMapper = faceBaselineMapper;
        this.profileIdentityRepository = profileIdentityRepository;
    }

    public FaceComparisonBaseline resolveOrThrow(long userId) {
        return resolve(userId).orElseThrow(() -> new ApiException(ApiCode.FACE_RECOGNITION_FAILED));
    }

    public Optional<FaceComparisonBaseline> resolve(long userId) {
        FaceBaselineCandidateRow latest = faceBaselineMapper.findLatestSuccessfulCompareByUserId(userId);
        if (latest != null
                && latest.candidateFaceEncryptedRef() != null
                && !latest.candidateFaceEncryptedRef().isBlank()) {
            return Optional.of(new FaceComparisonBaseline(
                    mapSourceType(latest.sourceType()),
                    latest.candidateFaceEncryptedRef(),
                    latest.comparedAt()
            ));
        }
        return identityBaseline(userId);
    }

    public boolean hasComparableBaseline(long userId) {
        return resolve(userId).isPresent();
    }

    private Optional<FaceComparisonBaseline> identityBaseline(long userId) {
        Optional<ProfileIdentityData> identity = profileIdentityRepository.findByUserId(userId);
        String ref = identity.map(ProfileIdentityData::facePhotoImageEncryptedRef).orElse(null);
        if (ref == null || ref.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new FaceComparisonBaseline(
                FaceComparisonBaseline.TYPE_IDENTITY,
                ref,
                null
        ));
    }

    private static String mapSourceType(String sourceType) {
        if (sourceType == null) {
            return FaceComparisonBaseline.TYPE_IDENTITY;
        }
        return switch (sourceType) {
            case "MOBILE_CHANGE" -> FaceComparisonBaseline.TYPE_MOBILE_CHANGE;
            case "DEVICE_SWITCH_LOGIN" -> FaceComparisonBaseline.TYPE_DEVICE_SWITCH_LOGIN;
            case "BANK_CARD_ADD" -> FaceComparisonBaseline.TYPE_BANK_CARD_ADD;
            default -> sourceType;
        };
    }
}
