package com.ty.hikingalone.application.email;

import com.ty.hikingalone.application.email.cmd.GetCodeCmd;
import com.ty.hikingalone.application.email.cmd.VerifyCodeCmd;
import com.ty.hikingalone.domain.email.entity.VerificationCode;
import com.ty.hikingalone.domain.email.repository.VerificationCodeStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮箱验证码应用服务：生成/存储/发送/校验验证码
 * <p>领域实体 {@link VerificationCode} 生成随机码与存储 key；验证码的「存/取/过期」
 * 通过领域仓库接口 {@link VerificationCodeStore} 完成（Redis 实现细节在基础设施层，
 * 本服务不感知）；邮件发送用 Spring 管理的 {@link JavaMailSender}（配置来自
 * application.yml 的 spring.mail.*）。超时/冷却时长由配置注入。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final VerificationCodeStore verificationCodeStore;

    /** 验证码有效期（秒），application.yml register.code.expire */
    @Value("${register.code.expire:300}")
    private int codeExpireSeconds;

    /** 同一邮箱发送冷却（秒），application.yml register.code.cool */
    @Value("${register.code.cool:60}")
    private int coolSeconds;

    /**
     * 发送验证码到邮箱：
     * <ol>
     *   <li>冷却期内直接返回 false（不发送，防刷）</li>
     *   <li>先写冷却标记（占位），再存验证码本体（供 verify 比对）</li>
     *   <li>通过 JavaMailSender 发邮件</li>
     * </ol>
     *
     * @return true=已发送；false=冷却中，未发送
     */
    public boolean sendCode(GetCodeCmd cmd) {
        VerificationCode code = VerificationCode.getCode(cmd.email());

        // 冷却标记存在 → 冷却中，不再发送
        if (verificationCodeStore.isCooling(code)) {
            log.info("验证码冷却中，跳过发送: {}", cmd.email());
            return false;
        }

        // 先写冷却标记（占位），再存验证码本体；过期时间各自独立
        verificationCodeStore.markCooling(code, coolSeconds);
        verificationCodeStore.saveCode(code, codeExpireSeconds);

        // 发送邮件：SimpleMailMessage 的 from 由 JavaMailSender 配置（spring.mail.username）自动带出
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(cmd.email());
        message.setSubject("Hiking Alone 验证码");
        message.setText("您的验证码是：" + code.getCode() + "，有效期 " + codeExpireSeconds / 60 + " 分钟，请勿泄露。");
        mailSender.send(message);

        log.info("验证码已发送至 {}，有效期 {}s", cmd.email(), codeExpireSeconds);
        return true;
    }

    /**
     * 校验验证码：与存储中该邮箱的验证码比对，一致则删除（一次性，防重放）并返回 true
     *
     * @return true=校验通过（验证码已消费）；false=错误或已过期
     */
    public boolean verify(VerifyCodeCmd cmd) {
        VerificationCode code = VerificationCode.ofEmail(cmd.email());
        String stored = verificationCodeStore.getCode(code);
        if (stored != null && stored.equals(cmd.code())) {
            verificationCodeStore.removeCode(code);
            log.info("验证码校验通过并消费: {}", cmd.email());
            return true;
        }
        log.warn("验证码校验失败: email={}", cmd.email());
        return false;
    }
}
