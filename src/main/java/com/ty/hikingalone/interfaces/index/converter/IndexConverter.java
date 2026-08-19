package com.ty.hikingalone.interfaces.index.converter;

import com.ty.hikingalone.interfaces.index.vo.HomeBannerVO;
import com.ty.hikingalone.interfaces.index.vo.NavMenuVO;
import com.ty.hikingalone.domain.index.entity.HomeBanner;
import com.ty.hikingalone.domain.index.entity.NavMenu;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 首页领域实体 → 视图对象 转换器
 */
@Component
public class IndexConverter {

    public NavMenuVO toNavMenuVO(NavMenu entity) {
        NavMenuVO vo = new NavMenuVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public List<NavMenuVO> toNavMenuVOList(List<NavMenu> entities) {
        return entities.stream().map(this::toNavMenuVO).toList();
    }

    public HomeBannerVO toHomeBannerVO(HomeBanner entity) {
        HomeBannerVO vo = new HomeBannerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    public List<HomeBannerVO> toHomeBannerVOList(List<HomeBanner> entities) {
        return entities.stream().map(this::toHomeBannerVO).toList();
    }
}
