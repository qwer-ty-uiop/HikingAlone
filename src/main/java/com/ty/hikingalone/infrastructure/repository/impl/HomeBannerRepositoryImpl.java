package com.ty.hikingalone.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.common.enums.StatusEnum;
import com.ty.hikingalone.domain.entity.HomeBanner;
import com.ty.hikingalone.domain.repository.HomeBannerRepository;
import com.ty.hikingalone.infrastructure.mapper.HomeBannerMapper;
import com.ty.hikingalone.infrastructure.po.HomeBannerPO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 首页横幅仓储实现
 */
@Repository
@RequiredArgsConstructor
public class HomeBannerRepositoryImpl implements HomeBannerRepository {

    private final HomeBannerMapper homeBannerMapper;

    @Override
    public List<HomeBanner> listEnabledBanners() {
        return homeBannerMapper.selectList(
                new LambdaQueryWrapper<HomeBannerPO>()
                        .eq(HomeBannerPO::getStatus, StatusEnum.ENABLED.getCode())
                        .orderByAsc(HomeBannerPO::getSort)
        ).stream().map(this::toEntity).toList();
    }

    private HomeBanner toEntity(HomeBannerPO po) {
        HomeBanner entity = new HomeBanner();
        BeanUtils.copyProperties(po, entity);
        return entity;
    }
}
