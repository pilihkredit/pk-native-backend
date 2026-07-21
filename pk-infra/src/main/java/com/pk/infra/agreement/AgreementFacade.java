package com.pk.infra.agreement;

import com.pk.core.agreement.UserAgreementRecordData;
import com.pk.core.agreement.port.UserAgreementRecordRepository;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.infra.auth.MobileNumberValidator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AgreementFacade {
    private static final int MAX_AGREEMENT_TYPE_LENGTH = 64;
    private static final int MAX_DEVICE_NO_LENGTH = 128;
    private static final int MAX_PARTNER_USER_ID_LENGTH = 64;
    private static final int MAX_ITEMS = 50;

    private final UserAgreementRecordRepository userAgreementRecordRepository;

    public AgreementFacade(UserAgreementRecordRepository userAgreementRecordRepository) {
        this.userAgreementRecordRepository = userAgreementRecordRepository;
    }

    public List<UserAgreementRecordData> createRecords(CreateCommand command) {
        validateCreate(command);
        Instant agreedAt = Instant.ofEpochMilli(command.clickedAtMs());
        String mobileNo = blankToNull(command.mobileNo());
        String deviceNo = command.deviceNo().trim();
        String partnerUserId = blankToNull(command.partnerUserId());

        List<UserAgreementRecordData> created = new ArrayList<>(command.items().size());
        for (AgreementItemCommand item : command.items()) {
            created.add(userAgreementRecordRepository.insert(
                    new UserAgreementRecordRepository.UserAgreementRecordInsert(
                            mobileNo,
                            partnerUserId,
                            deviceNo,
                            command.profileId(),
                            item.agreementType().trim(),
                            item.agreed(),
                            agreedAt,
                            command.clickedAtMs()
                    )
            ));
        }
        return List.copyOf(created);
    }

    public List<UserAgreementRecordData> latestByMobileNo(String mobileNo, List<String> agreementTypes) {
        if (mobileNo == null || mobileNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        String normalizedMobile = mobileNo.trim();
        if (!MobileNumberValidator.isValid(normalizedMobile)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }
        List<String> normalizedTypes = normalizeTypeFilter(agreementTypes);
        return userAgreementRecordRepository.findLatestByMobileNo(normalizedMobile, normalizedTypes);
    }

    private void validateCreate(CreateCommand command) {
        String mobileNo = blankToNull(command.mobileNo());
        if (mobileNo != null && !MobileNumberValidator.isValid(mobileNo)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }
        if (command.clickedAtMs() == null || command.clickedAtMs() <= 0L) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.deviceNo() == null || command.deviceNo().isBlank()
                || command.deviceNo().trim().length() > MAX_DEVICE_NO_LENGTH) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.partnerUserId() != null
                && !command.partnerUserId().isBlank()
                && command.partnerUserId().trim().length() > MAX_PARTNER_USER_ID_LENGTH) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.items() == null || command.items().isEmpty() || command.items().size() > MAX_ITEMS) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        Set<String> seenTypes = new HashSet<>();
        for (AgreementItemCommand item : command.items()) {
            if (item.agreementType() == null || item.agreementType().isBlank()
                    || item.agreementType().trim().length() > MAX_AGREEMENT_TYPE_LENGTH) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            if (!seenTypes.add(item.agreementType().trim())) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
        }
    }

    private static List<String> normalizeTypeFilter(List<String> agreementTypes) {
        if (agreementTypes == null || agreementTypes.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String type : agreementTypes) {
            if (type == null || type.isBlank() || type.trim().length() > MAX_AGREEMENT_TYPE_LENGTH) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            normalized.add(type.trim());
        }
        return List.copyOf(normalized);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public record CreateCommand(
            String mobileNo,
            String partnerUserId,
            String deviceNo,
            Long profileId,
            Long clickedAtMs,
            List<AgreementItemCommand> items
    ) {
    }

    public record AgreementItemCommand(String agreementType, Boolean agreed) {
    }
}
