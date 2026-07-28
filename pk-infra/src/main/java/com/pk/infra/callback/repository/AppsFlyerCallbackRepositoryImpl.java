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
}
