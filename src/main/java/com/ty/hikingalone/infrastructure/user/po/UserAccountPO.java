package com.ty.hikingalone.infrastructure.user.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("user")
public class UserAccountPO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
