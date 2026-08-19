package com.ty.hikingalone.interfaces.user.vo.command;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class UserCreateVO {

    private String email;
    private String username;

}
