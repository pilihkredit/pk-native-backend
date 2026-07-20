package com.pk.core.profile.port;

import com.pk.core.profile.UserDeviceOtherInfoData;

public interface UserDeviceOtherInfoRepository {
    void upsert(UserDeviceOtherInfoData data);

    void deleteByDeviceNo(String deviceNo);
}
