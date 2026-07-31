package com.pk.infra.callback.repository;

import com.pk.core.callback.port.AppsFlyerCallbackRepository;
import com.pk.infra.callback.mapper.AppsFlyerCallbackMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AppsFlyerCallbackRepositoryImpl implements AppsFlyerCallbackRepository {
    private final AppsFlyerCallbackMapper mapper;

    public AppsFlyerCallbackRepositoryImpl(AppsFlyerCallbackMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public long insert(AppsFlyerCallbackInsert insert) {
        AppsFlyerCallbackInsertParam param = AppsFlyerCallbackInsertParam.from(insert);
        mapper.insert(param);
        if (param.id == null) {
            throw new IllegalStateException("Failed to load inserted appsflyer_callback id");
        }
        return param.id;
    }

    @Override
    public int backfillBinding(String appsflyerId, Long userId, String deviceNo) {
        if (appsflyerId == null || appsflyerId.isBlank() || deviceNo == null || deviceNo.isBlank()) {
            return 0;
        }
        return mapper.backfillBinding(appsflyerId.trim(), userId, deviceNo.trim());
    }

    @Override
    public Optional<AppsFlyerCallbackData> findLatestByAppsflyerId(String appsflyerId) {
        if (appsflyerId == null || appsflyerId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.findLatestByAppsflyerId(appsflyerId.trim()));
    }

    @Override
    public Optional<AppsFlyerCallbackData> findLatestByAppsflyerIdAndEventName(String appsflyerId, String eventName) {
        if (appsflyerId == null || appsflyerId.isBlank() || eventName == null || eventName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.findLatestByAppsflyerIdAndEventName(appsflyerId.trim(), eventName.trim()));
    }

    @Override
    public Optional<AppsFlyerCallbackData> findLatestByAdvertisingId(String advertisingId) {
        if (advertisingId == null || advertisingId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.findLatestByAdvertisingId(advertisingId.trim()));
    }

    @Override
    public Optional<AppsFlyerCallbackData> findLatestByAdvertisingIdAndEventName(String advertisingId, String eventName) {
        if (advertisingId == null || advertisingId.isBlank() || eventName == null || eventName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.findLatestByAdvertisingIdAndEventName(advertisingId.trim(), eventName.trim()));
    }

    @Override
    public Optional<AppsFlyerCallbackData> findLatestByAndroidId(String androidId) {
        if (androidId == null || androidId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.findLatestByAndroidId(androidId.trim()));
    }

    @Override
    public Optional<AppsFlyerCallbackData> findLatestByAndroidIdAndEventName(String androidId, String eventName) {
        if (androidId == null || androidId.isBlank() || eventName == null || eventName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.findLatestByAndroidIdAndEventName(androidId.trim(), eventName.trim()));
    }

    @Override
    public Optional<AppsFlyerCallbackData> findLatestByDeviceNo(String deviceNo) {
        if (deviceNo == null || deviceNo.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.findLatestByDeviceNo(deviceNo.trim()));
    }

    @Override
    public Optional<AppsFlyerCallbackData> findLatestByDeviceNoAndEventName(String deviceNo, String eventName) {
        if (deviceNo == null || deviceNo.isBlank() || eventName == null || eventName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.findLatestByDeviceNoAndEventName(deviceNo.trim(), eventName.trim()));
    }

    @Override
    public Optional<AppsFlyerCallbackData> findLatestByDeviceNoAndConversionType(String deviceNo, String conversionType) {
        if (deviceNo == null || deviceNo.isBlank() || conversionType == null || conversionType.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                mapper.findLatestByDeviceNoAndConversionType(deviceNo.trim(), conversionType.trim())
        );
    }
}
