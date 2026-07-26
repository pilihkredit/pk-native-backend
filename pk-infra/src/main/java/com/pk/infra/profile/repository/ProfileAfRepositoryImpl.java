package com.pk.infra.profile.repository;

import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.port.ProfileAfRepository;
import com.pk.infra.profile.mapper.ProfileAfMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileAfRepositoryImpl implements ProfileAfRepository {
    private final ProfileAfMapper profileAfMapper;

    public ProfileAfRepositoryImpl(ProfileAfMapper profileAfMapper) {
        this.profileAfMapper = profileAfMapper;
    }

    @Override
    public Optional<ProfileAfData> findByRequestId(String requestId) {
        return Optional.ofNullable(profileAfMapper.findByRequestId(requestId)).map(this::toData);
    }

    @Override
    public Optional<ProfileAfData> findLatestByDeviceNo(String deviceNo) {
        if (deviceNo == null || deviceNo.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(profileAfMapper.findLatestByDeviceNo(deviceNo.trim())).map(this::toData);
    }

    @Override
    public long insert(ProfileAfData data) {
        ProfileAfRow row = toRow(data);
        profileAfMapper.insert(row);
        return row.id == null ? 0L : row.id;
    }

    @Override
    public void updateLastLenderInteraction(String requestId, Long externalInteractionId) {
        profileAfMapper.updateLastLenderInteraction(requestId, externalInteractionId);
    }

    @Override
    public void bindProfileIfNull(long id, long userId) {
        profileAfMapper.bindProfileIfNull(id, userId);
    }

    private ProfileAfData toData(ProfileAfRow row) {
        return new ProfileAfData(
                row.id,
                row.userId,
                row.deviceNo,
                row.appsflyerId,
                row.advertisingId,
                row.androidId,
                row.attributedTouchTime,
                row.gpClickTime,
                row.installTime,
                row.mediaSource,
                row.afPrt,
                row.afAdsetId,
                row.afAdset,
                row.afSiteid,
                row.afCId,
                row.campaign,
                row.appVersion,
                row.appId,
                row.deviceType,
                row.osVersion,
                row.countryCode,
                row.city,
                row.postalCode,
                row.ip,
                row.operator,
                row.deviceCategory,
                row.platform,
                row.deviceModel,
                row.idfv,
                row.idfa,
                row.afAd,
                row.afChannel,
                row.attributedTouchType,
                row.afAdId,
                row.afAdType,
                row.contributor1TouchType,
                row.contributor1TouchTime,
                row.contributor1AfPrt,
                row.contributor1MatchType,
                row.contributor1EngagementType,
                row.bundleId,
                row.matchType,
                row.gpInstallBegin,
                row.moduleStatus,
                row.requestId,
                row.externalInteractionId
        );
    }

    private static ProfileAfRow toRow(ProfileAfData data) {
        ProfileAfRow row = new ProfileAfRow();
        row.userId = data.userId();
        row.deviceNo = data.deviceNo();
        row.appsflyerId = data.appsflyerId();
        row.advertisingId = data.advertisingId();
        row.androidId = data.androidId();
        row.attributedTouchTime = data.attributedTouchTime();
        row.gpClickTime = data.gpClickTime();
        row.installTime = data.installTime();
        row.mediaSource = data.mediaSource();
        row.afPrt = data.afPrt();
        row.afAdsetId = data.afAdsetId();
        row.afAdset = data.afAdset();
        row.afSiteid = data.afSiteid();
        row.afCId = data.afCId();
        row.campaign = data.campaign();
        row.appVersion = data.appVersion();
        row.appId = data.appId();
        row.deviceType = data.deviceType();
        row.osVersion = data.osVersion();
        row.countryCode = data.countryCode();
        row.city = data.city();
        row.postalCode = data.postalCode();
        row.ip = data.ip();
        row.operator = data.operator();
        row.deviceCategory = data.deviceCategory();
        row.platform = data.platform();
        row.deviceModel = data.deviceModel();
        row.idfv = data.idfv();
        row.idfa = data.idfa();
        row.afAd = data.afAd();
        row.afChannel = data.afChannel();
        row.attributedTouchType = data.attributedTouchType();
        row.afAdId = data.afAdId();
        row.afAdType = data.afAdType();
        row.contributor1TouchType = data.contributor1TouchType();
        row.contributor1TouchTime = data.contributor1TouchTime();
        row.contributor1AfPrt = data.contributor1AfPrt();
        row.contributor1MatchType = data.contributor1MatchType();
        row.contributor1EngagementType = data.contributor1EngagementType();
        row.bundleId = data.bundleId();
        row.matchType = data.matchType();
        row.gpInstallBegin = data.gpInstallBegin();
        row.moduleStatus = data.moduleStatus();
        row.requestId = data.requestId();
        return row;
    }
}
