package com.ty.hikingalone.application.user;

import com.ty.hikingalone.application.user.cmd.QueryStatisticsCmd;
import com.ty.hikingalone.domain.user.entity.ProfileStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    public ProfileStatistics getProfileStatistics(QueryStatisticsCmd queryStatisticsCmd) {
        return null;
    }

}
