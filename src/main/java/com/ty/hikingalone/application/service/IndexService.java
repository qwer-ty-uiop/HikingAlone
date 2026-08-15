package com.ty.hikingalone.application.service;

import com.ty.hikingalone.controller.index.converter.IndexConverter;
import com.ty.hikingalone.controller.index.vo.HomeBodyVO;
import com.ty.hikingalone.domain.repository.HomeBannerRepository;
import com.ty.hikingalone.domain.repository.NavMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 首页应用服务：编排"组装首页数据"这个用例
 * <p>只负责编排领域仓储，不包含业务规则（业务规则在领域层）</p>
 */
@Service
@RequiredArgsConstructor
public class IndexService {

    private final NavMenuRepository navMenuRepository;
    private final HomeBannerRepository bannerRepository;
    private final IndexConverter converter;

    /**
     * 组装首页数据
     */
    public HomeBodyVO getHomeBody() {
        HomeBodyVO vo = new HomeBodyVO();
        vo.setNavMenus(converter.toNavMenuVOList(navMenuRepository.listMenus()));
        vo.setBanners(converter.toHomeBannerVOList(bannerRepository.listEnabledBanners()));
        return vo;
    }
}
