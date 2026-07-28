package com.pk.infra.callback.mapper;

import com.pk.core.callback.port.AppsFlyerCallbackRepository;
import com.pk.infra.callback.repository.AppsFlyerCallbackInsertParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppsFlyerCallbackMapper {
    int insert(AppsFlyerCallbackInsertParam param);

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByAppsflyerId(@Param("appsflyerId") String appsflyerId);

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByAppsflyerIdAndEventName(
            @Param("appsflyerId") String appsflyerId,
            @Param("eventName") String eventName
    );
}
