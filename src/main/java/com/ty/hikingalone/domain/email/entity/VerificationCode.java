package com.ty.hikingalone.domain.email.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Random;

/**
 * 邮箱验证码领域实体：纯业务对象，负责生成随机码、计算 Redis 存储 key
 * <p>不依赖任何 Spring/邮件框架（发送是应用层职责，见 EmailService）；超时/冷却时长由配置注入应用层</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VerificationCode {

    /** 验证码取值范围上界（不含），4 位数字即 [0, 10000) */
    public static final int CODE_BOUND = 10000;

    /** Redis key 前缀：验证码本体（value 存验证码） */
    public static final String REDIS_PREFIX_CODE = "email:code:";
    /** Redis key 前缀：冷却标记（value 仅占位，存在即冷却中） */
    public static final String REDIS_PREFIX_COOL = "email:cool:";

    private static final Random RANDOM = new Random();

    private String code;
    private String email;
    private String codeKey;
    private String coolKey;

    /**
     * 工厂方法：生成 4 位随机验证码，并预计算两个存储 key（邮箱为维度）
     * <p>发送场景使用：需要拿到新验证码本体去写存储 + 发邮件。</p>
     */
    public static VerificationCode getCode(String email) {
        VerificationCode verificationCode = ofEmail(email);
        verificationCode.code = String.format("%04d", RANDOM.nextInt(CODE_BOUND));
        return verificationCode;
    }

    /**
     * 工厂方法：仅按邮箱计算两个存储 key，不生成验证码
     * <p>校验场景使用：校验时只需要定位「该邮箱存的那条验证码」，不应生成新码；
     * 若误用 {@link #getCode} 会白白生成一个随机码再丢弃。</p>
     */
    public static VerificationCode ofEmail(String email) {
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.email = email;
        verificationCode.codeKey = REDIS_PREFIX_CODE + email;
        verificationCode.coolKey = REDIS_PREFIX_COOL + email;
        return verificationCode;
    }
}
