package com.ty.hikingalone.application.user;

import com.ty.hikingalone.application.email.EmailService;
import com.ty.hikingalone.application.email.cmd.VerifyCodeCmd;
import com.ty.hikingalone.application.user.cmd.UserChangePasswordCmd;
import com.ty.hikingalone.application.user.cmd.UserLoginCmd;
import com.ty.hikingalone.application.user.cmd.UserRegisterCmd;
import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.domain.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户模块应用服务：只做仓储查询、工厂调用与持久化编排，业务规则在领域实体
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final EmailService emailService;

    /**
     * 注册：先校验邮箱验证码，再查唯一性（仓储），最后交给实体工厂创建，落库
     */
    public UserAccount register(UserRegisterCmd cmd) {
        // 邮箱验证码必须校验通过（证明邮箱归属），通过后验证码即被消费，防止重放
        if (!emailService.verify(new VerifyCodeCmd(cmd.email(), cmd.code()))) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        if (userAccountRepository.findByUsername(cmd.username()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (cmd.email() != null && userAccountRepository.findByEmail(cmd.email()) != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        UserAccount account = UserAccount.register(cmd.username(), cmd.password(), cmd.email());
        userAccountRepository.save(account);
        return account;
    }

    /**
     * 登录：按邮箱取账号，密码校验交给实体；统一错误提示避免泄露账号是否存在
     */
    public UserAccount login(UserLoginCmd cmd) {
        UserAccount account = userAccountRepository.findByEmail(cmd.email());
        if (account == null || !account.verifyPassword(cmd.password())) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }
        return account;
    }

    /**
     * 修改密码：先校验邮箱验证码（证明邮箱归属），再取账号校验旧密码并更新，落库
     */
    public void changePassword(UserChangePasswordCmd cmd) {
        // 邮箱验证码必须校验通过（证明对邮箱的控制权），通过后验证码即被消费，防止重放
        if (!emailService.verify(new VerifyCodeCmd(cmd.email(), cmd.code()))) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        UserAccount account = userAccountRepository.findByEmail(cmd.email());
        if (account == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        account.changePassword(cmd.oldPassword(), cmd.newPassword());
        userAccountRepository.update(account);
    }

    /**
     * 按 id 查询账号（会话校验接口 /user/me 使用：会话中 userId 一定存在对应账号）
     */
    public UserAccount getUserById(Long id) {
        return userAccountRepository.findById(id);
    }
}