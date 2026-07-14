package com.pk.infra.repay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.repay.LenderRepayVa;
import com.pk.core.repay.port.LenderRepayVaPort;
import com.pk.core.repay.port.RepayVaSnapshotRepository;
import java.time.Instant;
import java.util.List;

public class RepayVaFacade {
    private final LenderRepayVaPort lenderRepayVaPort;
    private final RepayVaSnapshotRepository repayVaSnapshotRepository;
    private final ObjectMapper objectMapper;

    public RepayVaFacade(
            LenderRepayVaPort lenderRepayVaPort,
            RepayVaSnapshotRepository repayVaSnapshotRepository,
            ObjectMapper objectMapper
    ) {
        this.lenderRepayVaPort = lenderRepayVaPort;
        this.repayVaSnapshotRepository = repayVaSnapshotRepository;
        this.objectMapper = objectMapper;
    }

    public VaListResult listVas(long profileId, String partnerUserId) {
        LenderRepayVaPort.LenderRepayVaListResult lenderResult = lenderRepayVaPort.listVas(partnerUserId);
        persistSnapshots(profileId, lenderResult);
        return toListResult(lenderResult);
    }

    public VaDefaultResult setDefaultVa(long profileId, String partnerUserId, VaDefaultCommand command) {
        validateDefaultCommand(command);
        lenderRepayVaPort.setDefaultVa(new LenderRepayVaPort.LenderRepayVaDefaultCommand(
                partnerUserId,
                command.vaNo(),
                command.bankChannel()
        ));
        LenderRepayVaPort.LenderRepayVaListResult refreshed = lenderRepayVaPort.listVas(partnerUserId);
        persistSnapshots(profileId, refreshed);
        return new VaDefaultResult(command.vaNo(), true);
    }

    private void persistSnapshots(long profileId, LenderRepayVaPort.LenderRepayVaListResult lenderResult) {
        String snapshotNo = RepayNoGenerator.vaSnapshotNo();
        Instant fetchedAt = Instant.now();
        List<RepayVaSnapshotRepository.VaSnapshotInsert> inserts = lenderResult.vas().stream()
                .map(va -> new RepayVaSnapshotRepository.VaSnapshotInsert(
                        va.vaNo(),
                        va.bankCode(),
                        va.bankName(),
                        va.defaultFlag(),
                        va.disabled(),
                        serializeChannels(va)
                ))
                .toList();
        repayVaSnapshotRepository.replaceSnapshots(
                profileId,
                snapshotNo,
                inserts,
                lenderResult.requestJson(),
                lenderResult.rawResponseJson(),
                fetchedAt
        );
    }

    private String serializeChannels(LenderRepayVa va) {
        try {
            return objectMapper.writeValueAsString(va.bankChannels());
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private static void validateDefaultCommand(VaDefaultCommand command) {
        if (command == null
                || command.vaNo() == null
                || command.vaNo().isBlank()
                || command.vaNo().length() > 64
                || command.bankChannel() == null
                || command.bankChannel().isBlank()
                || command.bankChannel().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private VaListResult toListResult(LenderRepayVaPort.LenderRepayVaListResult lenderResult) {
        return new VaListResult(
                lenderResult.partnerUserId(),
                lenderResult.userId(),
                toVaInfo(lenderResult.defaultVa()),
                lenderResult.vas() == null
                        ? List.of()
                        : lenderResult.vas().stream().map(this::toVaInfo).toList()
        );
    }

    private VaInfoResult toVaInfo(LenderRepayVa va) {
        if (va == null) {
            return null;
        }
        List<VaChannelResult> channels = va.bankChannels() == null
                ? List.of()
                : va.bankChannels().stream()
                        .map(channel -> new VaChannelResult(
                                channel.bankChannel(),
                                channel.instruction(),
                                channel.defaultChannel()
                        ))
                        .toList();
        return new VaInfoResult(
                va.vaNo(),
                va.bankCode(),
                va.bankName(),
                va.bankType(),
                va.icon(),
                channels,
                va.defaultFlag(),
                va.disabled(),
                va.show()
        );
    }

    public record VaDefaultCommand(String vaNo, String bankChannel) {
    }

    public record VaListResult(
            String partnerUserId,
            String userId,
            VaInfoResult defaultVa,
            List<VaInfoResult> vas
    ) {
    }

    public record VaInfoResult(
            String vaNo,
            String bankCode,
            String bankName,
            Integer bankType,
            String icon,
            List<VaChannelResult> bankChannels,
            boolean defaultFlag,
            boolean disabled,
            boolean show
    ) {
    }

    public record VaChannelResult(
            String bankChannel,
            String instruction,
            boolean defaultChannel
    ) {
    }

    public record VaDefaultResult(String vaNo, boolean defaultFlag) {
    }
}
