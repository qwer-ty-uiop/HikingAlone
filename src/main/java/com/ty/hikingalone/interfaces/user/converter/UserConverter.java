package com.ty.hikingalone.interfaces.user.converter;

import com.ty.hikingalone.interfaces.user.dto.command.UserChangePasswordDTO;
import com.ty.hikingalone.interfaces.user.dto.command.UserCreateDTO;
import com.ty.hikingalone.interfaces.user.dto.query.UserLoginDTO;
import com.ty.hikingalone.interfaces.user.vo.command.UserChangePasswordVO;
import com.ty.hikingalone.interfaces.user.vo.command.UserCreateVO;
import com.ty.hikingalone.interfaces.user.vo.query.UserLoginVO;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public UserCreateVO toUserCreateDTO(UserCreateDTO userCreateDTO) {
        return UserCreateVO.builder()
                .email(userCreateDTO.getEmail())
                .username(userCreateDTO.getUsername())
                .build();
    }

    public UserLoginVO toUserLoginDTO(UserLoginDTO userLoginDTO) {
        return UserLoginVO.builder()
                .email(userLoginDTO.getEmail())
                .build();
    }

    public UserChangePasswordVO toUserChangePasswordDTO(UserChangePasswordDTO userChangePasswordDTO) {
        return UserChangePasswordVO.builder()
                .email(userChangePasswordDTO.getEmail())
                .build();
    }

}
