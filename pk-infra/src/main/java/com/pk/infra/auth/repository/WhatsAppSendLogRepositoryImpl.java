package com.pk.infra.auth.repository;

import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.port.WhatsAppSendLogRepository;
import com.pk.infra.auth.mapper.WhatsAppSendLogMapper;
import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class WhatsAppSendLogRepositoryImpl implements WhatsAppSendLogRepository {
    private final WhatsAppSendLogMapper whatsAppSendLogMapper;

    public WhatsAppSendLogRepositoryImpl(WhatsAppSendLogMapper whatsAppSendLogMapper) {
        this.whatsAppSendLogMapper = whatsAppSendLogMapper;
    }

    @Override
    public long countSince(String mobileNo, Instant sinceInclusive) {
        return whatsAppSendLogMapper.countSince(mobileNo, sinceInclusive);
    }

    @Override
    public long insert(WhatsAppSendLogEntry entry) {
        WhatsAppSendLogInsertParam param = new WhatsAppSendLogInsertParam(
                entry.userId().orElse(null),
                entry.mobileNo(),
                entry.deviceNo(),
                entry.otpToken(),
                entry.otpCode(),
                entry.purpose()
        );
        whatsAppSendLogMapper.insertParam(param);
        if (param.logId() == null) {
            throw new IllegalStateException("Failed to persist whatsapp_send_log row");
        }
        return param.logId();
    }

    @Override
    public void updateProviderResult(long logId, SmsSendResult result) {
        whatsAppSendLogMapper.updateProviderResult(
                logId,
                result.providerCode(),
                result.providerMessageId(),
                result.success(),
                result.errorCode(),
                result.errorMessage()
        );
    }

    public static class WhatsAppSendLogInsertParam {
        private Long userId;
        private String mobileNo;
        private String deviceNo;
        private String otpToken;
        private String otpCode;
        private String purpose;
        private Long logId;

        public WhatsAppSendLogInsertParam(
                Long userId,
                String mobileNo,
                String deviceNo,
                String otpToken,
                String otpCode,
                String purpose
        ) {
            this.userId = userId;
            this.mobileNo = mobileNo;
            this.deviceNo = deviceNo;
            this.otpToken = otpToken;
            this.otpCode = otpCode;
            this.purpose = purpose;
        }

        public Long getUserId() {
            return userId;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public String getDeviceNo() {
            return deviceNo;
        }

        public String getOtpToken() {
            return otpToken;
        }

        public String getOtpCode() {
            return otpCode;
        }

        public String getPurpose() {
            return purpose;
        }

        public Long getLogId() {
            return logId;
        }

        public void setLogId(Long logId) {
            this.logId = logId;
        }

        public Long logId() {
            return logId;
        }
    }
}
