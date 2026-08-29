package com.ty.hikingalone.interfaces.user.converter;

import com.ty.hikingalone.application.user.cmd.QueryStatisticsCmd;
import com.ty.hikingalone.domain.user.entity.ProfileStatistics;
import com.ty.hikingalone.interfaces.user.dto.query.ProfileStatisticsDTO;
import com.ty.hikingalone.interfaces.user.vo.query.ProfileStatisticsVO;
import org.springframework.stereotype.Component;

@Component
public class ProfileConverter {


    public QueryStatisticsCmd toQueryStatisticsCmd(ProfileStatisticsDTO dto) {
        return new QueryStatisticsCmd(dto.getUserId());
    }

    public ProfileStatisticsVO toProfileStatisticsVO(ProfileStatistics stats) {
        return ProfileStatisticsVO.builder().build();
    }

}
