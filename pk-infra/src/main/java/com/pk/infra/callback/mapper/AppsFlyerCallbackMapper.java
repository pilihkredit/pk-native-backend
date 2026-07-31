package com.pk.infra.callback.mapper;

import com.pk.core.callback.port.AppsFlyerCallbackRepository;
import com.pk.infra.callback.repository.AppsFlyerCallbackInsertParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppsFlyerCallbackMapper {
    int insert(AppsFlyerCallbackInsertParam param);

    int backfillBinding(
            @Param("appsflyerId") String appsflyerId,
            @Param("userId") Long userId,
            @Param("deviceNo") String deviceNo
    );

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByAppsflyerId(@Param("appsflyerId") String appsflyerId);

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByAppsflyerIdAndEventName(
            @Param("appsflyerId") String appsflyerId,
            @Param("eventName") String eventName
    );

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByAdvertisingId(@Param("advertisingId") String advertisingId);

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByAdvertisingIdAndEventName(
            @Param("advertisingId") String advertisingId,
            @Param("eventName") String eventName
    );

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByAndroidId(@Param("androidId") String androidId);

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByAndroidIdAndEventName(
            @Param("androidId") String androidId,
            @Param("eventName") String eventName
    );

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByDeviceNo(@Param("deviceNo") String deviceNo);

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByDeviceNoAndEventName(
            @Param("deviceNo") String deviceNo,
            @Param("eventName") String eventName
    );

    AppsFlyerCallbackRepository.AppsFlyerCallbackData findLatestByDeviceNoAndConversionType(
            @Param("deviceNo") String deviceNo,
            @Param("conversionType") String conversionType
    );
}
