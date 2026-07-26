package com.pk.infra.profile.repository;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.AccountCloseResult;
import com.pk.core.profile.IdentityDataRetention;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.infra.profile.mapper.UserProfileBindingMapper;
import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class UserProfileBindingRepositoryImpl implements UserProfileBindingRepository {
    private final UserProfileBindingMapper mapper;
    private final ProfileIdentityRepository profileIdentityRepository;

    public UserProfileBindingRepositoryImpl(
            UserProfileBindingMapper mapper,
            ProfileIdentityRepository profileIdentityRepository
    ) {
        this.mapper = mapper;
        this.profileIdentityRepository = profileIdentityRepository;
    }

    @Override
    public void recordLenderProfileSync(long userId, String externalUserId) {
        if (externalUserId != null && !externalUserId.isBlank()) {
            mapper.recordLenderProfileSyncWithExternalUser(userId, externalUserId.trim());
        } else {
            mapper.recordLenderProfileSyncWithoutExternalUser(userId);
        }
    }

    @Override
    public void updateKycStatus(long userId, String kycStatus) {
        mapper.updateKycStatus(userId, kycStatus);
    }

    @Override
    public AccountCloseResult closeAccount(long userId) {
        UserProfileBindingMapper.AccountClosureRow existing = mapper.findClosure(userId);
        if (existing == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        if (existing.deletedAt() != null) {
            Instant dataDeleteAt = existing.retentionUntil() != null
                    ? existing.retentionUntil()
                    : IdentityDataRetention.retentionUntil(existing.deletedAt());
            return new AccountCloseResult(existing.deletedAt(), dataDeleteAt, true);
        }

        Instant closedAt = Instant.now();
        Instant retentionUntil = IdentityDataRetention.retentionUntil(closedAt);
        int updated = mapper.closeAccount(userId, closedAt, retentionUntil);
        if (updated == 0) {
            UserProfileBindingMapper.AccountClosureRow raced = mapper.findClosure(userId);
            if (raced != null && raced.deletedAt() != null) {
                Instant dataDeleteAt = raced.retentionUntil() != null
                        ? raced.retentionUntil()
                        : IdentityDataRetention.retentionUntil(raced.deletedAt());
                return new AccountCloseResult(raced.deletedAt(), dataDeleteAt, true);
            }
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        profileIdentityRepository.scheduleRetentionAfterAccountClosure(userId, retentionUntil);
        return new AccountCloseResult(closedAt, retentionUntil, false);
    }
}
