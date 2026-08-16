package com.ty.hikingalone.infrastructure.index.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.index.entity.NavMenu;
import com.ty.hikingalone.domain.index.repository.NavMenuRepository;
import com.ty.hikingalone.infrastructure.index.mapper.NavMenuMapper;
import com.ty.hikingalone.infrastructure.index.po.NavMenuPO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 导航菜单仓储实现：领域接口的数据库实现，SQL 查询逻辑都在这里
 */
@Repository
@RequiredArgsConstructor
public class NavMenuRepositoryImpl implements NavMenuRepository {

    private final NavMenuMapper navMenuMapper;

    @Override
    public List<NavMenu> listMenus() {
        return navMenuMapper.selectList(
                new LambdaQueryWrapper<NavMenuPO>()
                        .orderByAsc(NavMenuPO::getSort)
        ).stream().map(this::toEntity).toList();
    }

    private NavMenu toEntity(NavMenuPO po) {
        NavMenu entity = new NavMenu();
        BeanUtils.copyProperties(po, entity);
        return entity;
    }
}
