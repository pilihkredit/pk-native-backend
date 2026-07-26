package com.pk.infra.ocr;

import com.pk.core.profile.ocr.OcrCallContextHolder;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import com.pk.infra.ocr.mapper.OcrVendorCallLogInsertParam;
import com.pk.infra.ocr.mapper.OcrVendorCallLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persist OCR vendor call audits. Insert failures are swallowed so business flow continues.
 */
public class OcrVendorCallLogWriterImpl implements OcrVendorCallLogWriter {
    private static final Logger log = LoggerFactory.getLogger(OcrVendorCallLogWriterImpl.class);

    private final OcrVendorCallLogMapper mapper;

    public OcrVendorCallLogWriterImpl(OcrVendorCallLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public long write(OcrVendorCallLogEntry entry) {
        if (entry == null) {
            return 0L;
        }
        try {
            OcrVendorCallLogInsertParam param = new OcrVendorCallLogInsertParam();
            param.setUserId(entry.userId());
            param.setPartnerUserId(entry.partnerUserId());
            param.setMobileNo(entry.mobileNo());
            param.setOperationType(entry.operationType() == null ? null : entry.operationType().name());
            param.setChannel(entry.channel() == null || entry.channel().isBlank() ? "advanceAi" : entry.channel());
            param.setTraceId(entry.traceId());
            param.setClientRequestId(entry.clientRequestId());
            param.setStatus(entry.status() == null ? null : entry.status().name());
            param.setApiCode(entry.apiCode());
            param.setVendorCode(entry.vendorCode());
            param.setVendorMessage(truncate(entry.vendorMessage(), 512));
            param.setScore(entry.score());
            param.setThreshold(entry.threshold());
            param.setEndpoint(entry.endpoint());
            param.setHttpStatus(entry.httpStatus());
            param.setDurationMs(entry.durationMs());
            param.setRequestJson(entry.requestJson());
            param.setResponseJson(entry.responseJson());
            param.setIdCardImageEncryptedRef(entry.idCardImageEncryptedRef());
            param.setLivenessImageEncryptedRef(entry.livenessImageEncryptedRef());
            mapper.insert(param);
            long id = param.getId() == null ? 0L : param.getId();
            if (id > 0) {
                OcrCallContextHolder.setLastVendorCallLogId(id);
            }
            return id;
        } catch (Exception exception) {
            log.warn(
                    "Failed to persist ocr_vendor_call_log operation={} status={} userId={}",
                    entry.operationType(),
                    entry.status(),
                    entry.userId(),
                    exception
            );
            return 0L;
        }
    }

    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
