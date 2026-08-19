package com.ty.hikingalone.application.user;

import com.ty.hikingalone.application.user.cmd.UserChangePasswordCmd;
import com.ty.hikingalone.application.user.cmd.UserLoginCmd;
import com.ty.hikingalone.application.user.cmd.UserRegisterCmd;
import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.domain.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户模块应用服务：接收应用层命令对象，编排领域逻辑与持久化，不依赖接口层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;

    /**
     * 注册：用户名/邮箱唯一性校验后创建账号，返回新账号
     */
    public UserAccount register(UserRegisterCmd cmd) {
        if (userAccountRepository.findByUsername(cmd.username()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (cmd.email() != null && userAccountRepository.findByEmail(cmd.email()) != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        UserAccount account = UserAccount.builder()
                .username(cmd.username())
                .password(cmd.password())
                .email(cmd.email())
                .build();
        userAccountRepository.save(account);
        return account;
    }

    /**
     * 登录：按邮箱查账号并校验密码，返回账号信息
     */
    public UserAccount login(UserLoginCmd cmd) {
        UserAccount account = userAccountRepository.findByEmail(cmd.email());
        if (account == null || !account.getPassword().equals(cmd.password())) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }
        return account;
    }

    /**
     * 修改密码：校验旧密码后覆盖新密码
     */
    public void changePassword(UserChangePasswordCmd cmd) {
        UserAccount account = userAccountRepository.findByEmail(cmd.email());
        if (account == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        if (!account.getPassword().equals(cmd.oldPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }
        account.setPassword(cmd.newPassword());
        userAccountRepository.update(account);
    }
}
