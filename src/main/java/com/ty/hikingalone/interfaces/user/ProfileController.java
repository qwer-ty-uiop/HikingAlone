package com.ty.hikingalone.interfaces.user;


import com.ty.hikingalone.application.user.ProfileService;
import com.ty.hikingalone.application.user.cmd.QueryStatisticsCmd;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.domain.user.entity.ProfileStatistics;
import com.ty.hikingalone.interfaces.user.converter.ProfileConverter;
import com.ty.hikingalone.interfaces.user.dto.query.ProfileStatisticsDTO;
import com.ty.hikingalone.interfaces.user.vo.query.ProfileStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileConverter profileConverter;

    /**
     * TODO 获取账户数据统计
     */
    public Result<ProfileStatisticsVO> getStatistics(ProfileStatisticsDTO dto) {
        QueryStatisticsCmd queryStatisticsCmd = profileConverter.toQueryStatisticsCmd(dto);
        ProfileStatistics profileStatistics = profileService.getProfileStatistics(queryStatisticsCmd);
        return Result.success(profileConverter.toProfileStatisticsVO(profileStatistics));
    }

    /**
     * TODO 获取徒步轨迹记录
     */
    public Result<Void> getHikingRecords() {
        return Result.success();
    }

    /**
     * TODO 获取计划清单
     */
    public Result<Void> getTodoList() {
        return Result.success();
    }

    /**
     * TODO 获取收藏列表
     */
    public Result<Void> getCollections() {
        return Result.success();
    }


}
