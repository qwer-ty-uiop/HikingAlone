package com.ty.hikingalone.interfaces.index.vo;

import lombok.Data;

/**
 * 导航菜单视图对象
 */
@Data
public class NavMenuVO {

    private Long id;

    private String name;

    private String linkUrl;

    private Long parentId;
}
