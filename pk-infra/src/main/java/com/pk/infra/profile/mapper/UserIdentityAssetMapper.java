package com.pk.infra.profile.mapper;
import com.pk.infra.profile.repository.UserIdentityAssetInsertParam;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface UserIdentityAssetMapper {
    int insert(UserIdentityAssetInsertParam param);
}
