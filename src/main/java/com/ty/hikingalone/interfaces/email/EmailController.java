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
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<EmailSendVO> sendCode(@Valid @RequestBody EmailDTO emailDTO, HttpServletRequest request) {
        boolean sent = emailService.sendCode(emailConverter.toSendEmailCmd(emailDTO, clientIp(request)));
        return Result.success(emailConverter.toEmailSendVO(sent));
    }

    /**
     * 客户端真实 IP：单可信代理（nginx）拓扑下取 X-Forwarded-For 的<b>最后一个</b>元素——
     * 该元素由 nginx 追加（$proxy_add_x_forwarded_for = 请求方带入的 XFF + remote_addr），
     * 客户端伪造的值只会排在其前面，不可伪造；无 XFF（直连）则取 remoteAddr
     */
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] chain = forwarded.split(",");
            return chain[chain.length - 1].trim();
        }
        return request.getRemoteAddr();
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
