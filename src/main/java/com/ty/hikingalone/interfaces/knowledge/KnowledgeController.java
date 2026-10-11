package com.ty.hikingalone.interfaces.knowledge;

import com.ty.hikingalone.application.knowledge.KnowledgeService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.knowledge.converter.KnowledgeConverter;
import com.ty.hikingalone.interfaces.knowledge.dto.command.KnowledgeIdDTO;
import com.ty.hikingalone.interfaces.knowledge.dto.command.KnowledgeSaveDTO;
import com.ty.hikingalone.interfaces.knowledge.dto.command.KnowledgeUpdateDTO;
import com.ty.hikingalone.interfaces.knowledge.vo.query.KnowledgeNoteVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识库控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final KnowledgeConverter knowledgeConverter;

    /**
     * 新建笔记
     */
    @PostMapping
    public Result<KnowledgeNoteVO> create(@Valid @RequestBody KnowledgeSaveDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(knowledgeService.create(userId, knowledgeConverter.toSaveCmd(dto)));
    }

    /**
     * 编辑笔记（非 null 覆盖，可置顶）
     */
    @PostMapping("/update")
    public Result<KnowledgeNoteVO> update(@Valid @RequestBody KnowledgeUpdateDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(knowledgeService.update(userId, knowledgeConverter.toSaveCmd(dto)));
    }

    /**
     * 删除笔记
     */
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody KnowledgeIdDTO dto) {
        knowledgeService.delete(LoginUserContext.getUserId(), knowledgeConverter.toIdCmd(dto));
        return Result.success();
    }

    /**
     * 笔记列表（关键词 + 分类过滤，置顶在前）
     */
    @GetMapping("/list")
    public Result<List<KnowledgeNoteVO>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer category) {
        return Result.success(knowledgeService.list(LoginUserContext.getUserId(), keyword, category));
    }
}
