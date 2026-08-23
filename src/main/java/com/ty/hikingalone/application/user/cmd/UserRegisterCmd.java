package com.ty.hikingalone.application.user.cmd;

public record UserRegisterCmd(String username, String password, String email, String code) {
}
