package com.ty.hikingalone.domain.repository;

import com.ty.hikingalone.domain.entity.NavMenu;

import java.util.List;

/**
 * 导航菜单仓储接口
 * <p>领域层只声明"需要什么数据"，不关心如何查询，由基础设施层实现</p>
 */
public interface NavMenuRepository {

    /**
     * 查询所有启用的导航菜单，按排序号升序
     */
    List<NavMenu> listMenus();
}
