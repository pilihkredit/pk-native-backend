package com.pk.infra.profile.repository;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.infra.profile.mapper.ProfileDeviceMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository
public class ProfileDeviceRepositoryImpl implements ProfileDeviceRepository {
    private final ProfileDeviceMapper mapper;
    public ProfileDeviceRepositoryImpl(ProfileDeviceMapper mapper) { this.mapper = mapper; }
    @Override public boolean existsByUserId(long userId) { return mapper.existsByUserId(userId); }
    @Override public Optional<ProfileDeviceData> findByDeviceNo(String deviceNo) {
        return Optional.ofNullable(mapper.findByDeviceNo(deviceNo.trim()));
    }
    @Override public void upsertByDeviceNo(ProfileDeviceData data) { mapper.upsertByDeviceNo(data); }
}
