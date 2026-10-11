package com.ty.hikingalone.interfaces.achievement.vo.query;

import lombok.Data;

import java.util.List;

/**
 * 徽章分组 VO
 */
@Data
public class AchievementGroupVO {

    /** 分组名（步履/里程/爬升/单日/自律） */
    private String group;

    /** 分组内徽章 */
    private List<AchievementVO> achievements;
}
