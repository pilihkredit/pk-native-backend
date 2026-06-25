package com.pk.infra.reference;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.reference.AreaReference;
import com.pk.core.reference.port.LenderAreaPort;
import com.pk.core.reference.port.RefAreaRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AreaReferenceFacade {
    private final RefAreaRepository refAreaRepository;
    private final LenderAreaPort lenderAreaPort;
    private final Duration areaCacheTtl;

    public AreaReferenceFacade(
            RefAreaRepository refAreaRepository,
            LenderAreaPort lenderAreaPort,
            ReferenceProperties referenceProperties
    ) {
        this.refAreaRepository = refAreaRepository;
        this.lenderAreaPort = lenderAreaPort;
        this.areaCacheTtl = referenceProperties.areaCacheTtl();
    }

    public List<AreaReference> listAreas(String parentCode) {
        String normalizedParent = AreaReferenceSupport.normalizeParentCode(parentCode);
        if (!normalizedParent.isEmpty() && !refAreaRepository.existsActiveAreaCode(normalizedParent)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        List<AreaReference> cached = refAreaRepository.findActiveByParentCode(normalizedParent);
        if (!cached.isEmpty() && !isStale(refAreaRepository.findLatestSyncedAtByParent(normalizedParent))) {
            return cached;
        }
        syncFromLender(normalizedParent);
        return refAreaRepository.findActiveByParentCode(normalizedParent);
    }

    public void validateResidentialHierarchy(String provinceCode, String cityCode, String districtCode) {
        if (isBlank(provinceCode) || isBlank(cityCode) || isBlank(districtCode)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        AreaReference province = requireActiveArea(provinceCode.trim(), 1, "");
        AreaReference city = requireActiveArea(cityCode.trim(), 2, province.areaCode());
        AreaReference district = requireActiveArea(districtCode.trim(), 3, city.areaCode());
        if (!Objects.equals(city.parentCode(), province.areaCode())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (!Objects.equals(district.parentCode(), city.areaCode())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private AreaReference requireActiveArea(String areaCode, int expectedLevel, String parentForSync) {
        Optional<AreaReference> cached = refAreaRepository.findActiveByCode(areaCode);
        if (cached.isPresent()) {
            return validateLevel(cached.get(), expectedLevel);
        }
        listAreas(parentForSync);
        return validateLevel(
                refAreaRepository.findActiveByCode(areaCode)
                        .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS)),
                expectedLevel
        );
    }

    private static AreaReference validateLevel(AreaReference area, int expectedLevel) {
        if (area.level() != expectedLevel) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return area;
    }

    private void syncFromLender(String parentCode) {
        try {
            List<AreaReference> areas = lenderAreaPort.listAreas(parentCode.isEmpty() ? null : parentCode);
            if (areas == null || areas.isEmpty()) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
            }
            int childLevel = resolveChildLevel(parentCode);
            List<AreaReference> normalized = areas.stream()
                    .map(area -> new AreaReference(
                            area.areaCode(),
                            area.areaName(),
                            AreaReferenceSupport.normalizeParentCode(area.parentCode()),
                            childLevel
                    ))
                    .toList();
            refAreaRepository.replaceChildrenByParent(parentCode, normalized, Instant.now());
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    public void refreshFromLender(String parentCode) {
        String normalizedParent = AreaReferenceSupport.normalizeParentCode(parentCode);
        syncFromLender(normalizedParent);
    }

    private int resolveChildLevel(String normalizedParent) {
        if (normalizedParent.isEmpty()) {
            return 1;
        }
        return refAreaRepository.findActiveByCode(normalizedParent)
                .map(parent -> parent.level() + 1)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));
    }

    private boolean isStale(Optional<Instant> latestSyncedAt) {
        if (latestSyncedAt.isEmpty()) {
            return true;
        }
        return latestSyncedAt.get().plus(areaCacheTtl).isBefore(Instant.now());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
