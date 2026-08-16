package com.ty.hikingalone.domain.index.entity;

import lombok.Data;

/**
 * 导航菜单领域实体
 */
@Data
public class NavMenu {

    private Long id;

    private String name;

    private String linkUrl;

    private Long parentId;

    private Integer sort;
}
