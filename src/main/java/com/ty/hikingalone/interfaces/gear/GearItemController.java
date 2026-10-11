package com.ty.hikingalone.interfaces.gear;

import com.ty.hikingalone.application.gear.GearItemService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.gear.converter.GearConverter;
import com.ty.hikingalone.interfaces.gear.dto.command.GearIdDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearItemSaveDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearItemUpdateDTO;
import com.ty.hikingalone.interfaces.gear.vo.query.GearItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 装备台账控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/gear/items")
@RequiredArgsConstructor
public class GearItemController {

    private final GearItemService gearItemService;
    private final GearConverter gearConverter;

    /**
     * 全量装备列表（在役在前）
     */
    @GetMapping
    public Result<List<GearItemVO>> list() {
        return Result.success(gearItemService.list(LoginUserContext.getUserId()));
    }

    /**
     * 新建装备（默认在役）
     */
    @PostMapping
    public Result<GearItemVO> create(@Valid @RequestBody GearItemSaveDTO dto) {
        return Result.success(gearItemService.create(LoginUserContext.getUserId(), gearConverter.toItemCmd(dto)));
    }

    /**
     * 编辑装备（全量替换台账内容）
     */
    @PostMapping("/update")
    public Result<GearItemVO> update(@Valid @RequestBody GearItemUpdateDTO dto) {
        return Result.success(gearItemService.update(LoginUserContext.getUserId(), gearConverter.toItemCmd(dto)));
    }

    /**
     * 删除装备（物理删除，不影响清单条目快照）
     */
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody GearIdDTO dto) {
        gearItemService.delete(LoginUserContext.getUserId(), gearConverter.toIdCmd(dto));
        return Result.success();
    }
}
