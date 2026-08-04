package com.pk.infra.launch.repository;

import com.pk.core.launch.AppLaunchRecord;
import com.pk.core.launch.port.AppLaunchRepository;
import com.pk.infra.launch.mapper.AppLaunchMapper;
import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class AppLaunchRepositoryImpl implements AppLaunchRepository {
    private final AppLaunchMapper appLaunchMapper;

    public AppLaunchRepositoryImpl(AppLaunchMapper appLaunchMapper) {
        this.appLaunchMapper = appLaunchMapper;
    }

    @Override
    public boolean record(AppLaunchRecord record) {
        return appLaunchMapper.insertEvent(record) > 0;
    }

    @Override
    public long countEvents(Instant fromInclusive, Instant toExclusive, String deviceNo) {
        return appLaunchMapper.countEvents(fromInclusive, toExclusive, deviceNo);
    }

}
