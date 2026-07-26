package com.pk.infra.attribution.repository;

import com.pk.core.attribution.port.AdjustEventRecordRepository;
import com.pk.infra.attribution.mapper.AdjustEventRecordMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AdjustEventRecordRepositoryImpl implements AdjustEventRecordRepository {
    private final AdjustEventRecordMapper mapper;

    public AdjustEventRecordRepositoryImpl(AdjustEventRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public long insert(AdjustEventRecordInsert record) {
        AdjustEventRecordInsertParam param = new AdjustEventRecordInsertParam(
                record.callbackEventId(),
                record.partnerUserId(),
                record.userId(),
                record.deviceUuid(),
                record.eventName(),
                record.appToken(),
                record.idfa(),
                record.idfv(),
                record.gpsAdid(),
                record.adid(),
                record.eventTimestamp(),
                record.status(),
                record.retryCount(),
                record.extraParams()
        );
        mapper.insert(param);
        return param.getId() == null ? 0L : param.getId();
    }

    @Override
    public void updateStatus(long id, int status, String response, String errorMessage, int retryCount) {
        mapper.updateStatus(id, status, response, errorMessage, retryCount);
    }
}
