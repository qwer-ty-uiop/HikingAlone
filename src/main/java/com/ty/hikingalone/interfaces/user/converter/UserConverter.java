package com.ty.hikingalone.interfaces.user.converter;

import com.ty.hikingalone.application.user.cmd.UserChangePasswordCmd;
import com.ty.hikingalone.application.user.cmd.UserLoginCmd;
import com.ty.hikingalone.application.user.cmd.UserRegisterCmd;
import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.interfaces.user.dto.command.UserChangePasswordDTO;
import com.ty.hikingalone.interfaces.user.dto.command.UserCreateDTO;
import com.ty.hikingalone.interfaces.user.dto.query.UserLoginDTO;
import com.ty.hikingalone.interfaces.user.vo.command.UserCreateVO;
import com.ty.hikingalone.interfaces.user.vo.query.UserLoginVO;
import org.springframework.stereotype.Component;

/**
 * 用户模块接口层转换器：HTTP DTO → 应用层命令；领域实体 → 视图对象
 */
@Component
public class UserConverter {

    public UserRegisterCmd toRegisterCmd(UserCreateDTO userCreateDTO) {
        return new UserRegisterCmd(
                userCreateDTO.getUsername(),
                userCreateDTO.getPassword(),
                userCreateDTO.getEmail(),
                userCreateDTO.getCode());
    }

    public UserLoginCmd toLoginCmd(UserLoginDTO userLoginDTO) {
        return new UserLoginCmd(userLoginDTO.getEmail(), userLoginDTO.getPassword());
    }

    public UserChangePasswordCmd toChangePasswordCmd(UserChangePasswordDTO userChangePasswordDTO) {
        return new UserChangePasswordCmd(
                userChangePasswordDTO.getEmail(),
                userChangePasswordDTO.getOldPassword(),
                userChangePasswordDTO.getNewPassword(),
                userChangePasswordDTO.getCode());
    }

    public UserCreateVO toUserCreateVO(UserAccount account) {
        return UserCreateVO.builder()
                .email(account.getEmail())
                .username(account.getUsername())
                .build();
    }

    public UserLoginVO toUserLoginVO(UserAccount account) {
        return UserLoginVO.builder()
                .email(account.getEmail())
                .build();
    }
}
