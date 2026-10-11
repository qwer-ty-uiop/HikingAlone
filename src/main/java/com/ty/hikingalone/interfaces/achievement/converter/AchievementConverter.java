package com.ty.hikingalone.interfaces.achievement.converter;

import com.ty.hikingalone.domain.achievement.entity.Achievement;
import com.ty.hikingalone.interfaces.achievement.vo.query.AchievementGroupVO;
import com.ty.hikingalone.interfaces.achievement.vo.query.AchievementVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 成就徽章 值对象 → VO 转换器
 */
@Component
public class AchievementConverter {

    /**
     * 分组装配（LinkedHashMap 保序）
     */
    public List<AchievementGroupVO> toGroupVOList(Map<String, List<Achievement>> grouped) {
        List<AchievementGroupVO> groups = new ArrayList<>();
        for (Map.Entry<String, List<Achievement>> entry : grouped.entrySet()) {
            AchievementGroupVO vo = new AchievementGroupVO();
            vo.setGroup(entry.getKey());
            vo.setAchievements(entry.getValue().stream().map(this::toVO).toList());
            groups.add(vo);
        }
        return groups;
    }

    public AchievementVO toVO(Achievement achievement) {
        AchievementVO vo = new AchievementVO();
        vo.setCode(achievement.code());
        vo.setName(achievement.name());
        vo.setDescription(achievement.description());
        vo.setAchieved(achievement.achieved());
        vo.setCurrent(achievement.current());
        vo.setTarget(achievement.target());
        vo.setProgressPercent(achievement.progressPercent());
        return vo;
    }
}
