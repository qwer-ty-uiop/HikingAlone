package com.ty.hikingalone.domain.index.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页横幅领域实体
 */
@Data
public class HomeBanner {

    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
