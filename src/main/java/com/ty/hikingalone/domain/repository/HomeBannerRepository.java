package com.ty.hikingalone.domain.repository;

import com.ty.hikingalone.domain.entity.HomeBanner;

import java.util.List;

/**
 * 首页横幅仓储接口
 */
public interface HomeBannerRepository {

    /**
     * 查询所有启用的首页横幅，按排序号升序
     */
    List<HomeBanner> listEnabledBanners();
}
