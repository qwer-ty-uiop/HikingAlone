package com.ty.hikingalone.application.user.cmd;

public record UserChangePasswordCmd(String email, String oldPassword, String newPassword, String code) {
}
