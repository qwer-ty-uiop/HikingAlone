package com.ty.hikingalone.controller.index.vo;

import lombok.Data;

/**
 * 首页横幅视图对象
 */
@Data
public class HomeBannerVO {

    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;
}
