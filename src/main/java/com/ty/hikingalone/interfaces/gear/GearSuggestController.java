package com.ty.hikingalone.interfaces.gear;

import com.ty.hikingalone.application.gear.GearSuggestService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.gear.dto.command.GearSuggestDTO;
import com.ty.hikingalone.interfaces.gear.vo.query.GearSuggestVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 装备建议控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/gear/suggest")
@RequiredArgsConstructor
public class GearSuggestController {

    private final GearSuggestService gearSuggestService;

    /**
     * 生成装备建议（LLM 优先，未配置/失败自动降级内置规则引擎，source 字段区分）
     */
    @PostMapping
    public Result<GearSuggestVO> suggest(@Valid @RequestBody GearSuggestDTO dto) {
        return Result.success(gearSuggestService.suggest(LoginUserContext.getUserId(),
                dto.getRouteId(), dto.getDays(), dto.getDistanceKm(), dto.getMonth(), dto.getExtra()));
    }
}
