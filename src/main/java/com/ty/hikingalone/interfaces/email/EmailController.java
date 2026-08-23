package com.ty.hikingalone.interfaces.email;

import com.ty.hikingalone.application.email.EmailService;
import com.ty.hikingalone.application.email.cmd.VerifyCodeCmd;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.email.converter.EmailConverter;
import com.ty.hikingalone.interfaces.email.dto.EmailDTO;
import com.ty.hikingalone.interfaces.email.dto.EmailVerifyDTO;
import com.ty.hikingalone.interfaces.email.vo.EmailSendVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱验证码控制器：发送验证码、校验验证码
 * <p>发送接口不返回验证码本体（只能通过邮件收到），防止验证码被绕过/利用</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;
    private final EmailConverter emailConverter;

    /**
     * 发送验证码：POST /email/code
     * <p>返回 sent=true 表示已发送；sent=false 表示 60s 冷却中未发送（前端提示稍后再试）</p>
     */
    @PostMapping("/code")
    public Result<EmailSendVO> sendCode(@Valid @RequestBody EmailDTO emailDTO) {
        boolean sent = emailService.sendCode(emailConverter.toSendEmailCmd(emailDTO));
        return Result.success(emailConverter.toEmailSendVO(sent));
    }

    /**
     * 校验验证码：POST /email/verify
     * <p>供前端在注册/改密提交前校验，避免把错误验证码一路带到业务层</p>
     */
    @PostMapping("/verify")
    public Result<Boolean> verifyCode(@Valid @RequestBody EmailVerifyDTO emailVerifyDTO) {
        VerifyCodeCmd cmd = emailConverter.toVerifyCodeCmd(emailVerifyDTO);
        return Result.success(emailService.verify(cmd));
    }

}
