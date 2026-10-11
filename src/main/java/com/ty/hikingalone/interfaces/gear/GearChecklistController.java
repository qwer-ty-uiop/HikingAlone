package com.ty.hikingalone.interfaces.gear;

import com.ty.hikingalone.application.gear.GearChecklistService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.gear.converter.GearConverter;
import com.ty.hikingalone.interfaces.gear.dto.command.ChecklistItemAddDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.ChecklistItemUpdateDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearChecklistSaveDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearChecklistUpdateDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearIdDTO;
import com.ty.hikingalone.interfaces.gear.vo.query.GearChecklistVO;
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
 * 打包清单控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/gear/checklists")
@RequiredArgsConstructor
public class GearChecklistController {

    private final GearChecklistService checklistService;
    private final GearConverter gearConverter;

    /**
     * 全量清单（含条目）
     */
    @GetMapping
    public Result<List<GearChecklistVO>> list() {
        return Result.success(checklistService.list(LoginUserContext.getUserId()));
    }

    /**
     * 新建空清单
     */
    @PostMapping
    public Result<GearChecklistVO> create(@Valid @RequestBody GearChecklistSaveDTO dto) {
        return Result.success(checklistService.create(LoginUserContext.getUserId(), gearConverter.toChecklistCmd(dto)));
    }

    /**
     * 编辑清单头（非 null 覆盖）
     */
    @PostMapping("/update")
    public Result<GearChecklistVO> update(@Valid @RequestBody GearChecklistUpdateDTO dto) {
        return Result.success(checklistService.update(LoginUserContext.getUserId(), gearConverter.toChecklistCmd(dto)));
    }

    /**
     * 删除清单（级联删条目）
     */
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody GearIdDTO dto) {
        checklistService.delete(LoginUserContext.getUserId(), gearConverter.toIdCmd(dto));
        return Result.success();
    }

    /**
     * 复制清单（标题追加「 · 副本」，勾选清零）
     */
    @PostMapping("/copy")
    public Result<GearChecklistVO> copy(@Valid @RequestBody GearIdDTO dto) {
        return Result.success(checklistService.copy(LoginUserContext.getUserId(), gearConverter.toIdCmd(dto)));
    }

    /**
     * 加条目
     */
    @PostMapping("/items/add")
    public Result<GearChecklistVO> addItem(@Valid @RequestBody ChecklistItemAddDTO dto) {
        return Result.success(checklistService.addItem(LoginUserContext.getUserId(), gearConverter.toItemAddCmd(dto)));
    }

    /**
     * 更新条目（勾选/改名/改克重）
     */
    @PostMapping("/items/update")
    public Result<GearChecklistVO> updateItem(@Valid @RequestBody ChecklistItemUpdateDTO dto) {
        return Result.success(checklistService.updateItem(LoginUserContext.getUserId(), gearConverter.toItemUpdateCmd(dto)));
    }

    /**
     * 删条目
     */
    @PostMapping("/items/delete")
    public Result<GearChecklistVO> deleteItem(@Valid @RequestBody GearIdDTO dto) {
        return Result.success(checklistService.deleteItem(LoginUserContext.getUserId(), gearConverter.toIdCmd(dto)));
    }
}
