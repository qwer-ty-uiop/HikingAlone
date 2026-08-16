package com.ty.hikingalone.infrastructure.index.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 导航菜单持久化对象，与 nav_menu 表字段一一对应
 */
@Data
@TableName("nav_menu")
public class NavMenuPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String linkUrl;

    private Long parentId;

    private Integer sort;
}
