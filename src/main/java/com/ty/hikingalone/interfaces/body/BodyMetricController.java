package com.ty.hikingalone.interfaces.body;

import com.ty.hikingalone.application.body.BodyMetricService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.body.converter.BodyMetricConverter;
import com.ty.hikingalone.interfaces.body.dto.command.BodyMetricRecordDTO;
import com.ty.hikingalone.interfaces.body.vo.query.BodyMetricRecentVO;
import com.ty.hikingalone.interfaces.body.vo.query.BodyMetricRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 身体数据模块控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/body")
@RequiredArgsConstructor
public class BodyMetricController {

    private final BodyMetricService bodyMetricService;
    private final BodyMetricConverter bodyMetricConverter;

    /**
     * 记录/更新当日身体数据（每日一条，同日重复提交覆盖更新）
     */
    @PostMapping("/record")
    public Result<BodyMetricRecordVO> record(@Valid @RequestBody BodyMetricRecordDTO dto) {
        return Result.success(bodyMetricService.record(LoginUserContext.getUserId(), bodyMetricConverter.toRecordCmd(dto)));
    }

    /**
     * 最近 N 天记录（升序）+ 汇总统计；days 缺省 30
     */
    @GetMapping("/recent")
    public Result<BodyMetricRecentVO> recent(@RequestParam(required = false) Integer days) {
        return Result.success(bodyMetricService.recent(LoginUserContext.getUserId(), days));
    }
}
