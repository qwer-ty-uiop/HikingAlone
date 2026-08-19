package com.ty.hikingalone.domain.user.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class UserAccount {
    private Long id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
