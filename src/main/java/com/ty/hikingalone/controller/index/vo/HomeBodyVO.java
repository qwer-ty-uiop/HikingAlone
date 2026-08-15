package com.ty.hikingalone.controller.index.vo;

import lombok.Data;

import java.util.List;

/**
 * 首页数据视图对象，对应 GET /home 接口返回体
 */
@Data
public class HomeBodyVO {

    /**
     * 导航菜单
     */
    private List<NavMenuVO> navMenus;

    /**
     * 首页横幅
     */
    private List<HomeBannerVO> banners;
}
