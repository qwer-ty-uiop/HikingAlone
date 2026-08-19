package com.ty.hikingalone.interfaces.user.vo.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserChangePasswordVO {
    private String email;
}
