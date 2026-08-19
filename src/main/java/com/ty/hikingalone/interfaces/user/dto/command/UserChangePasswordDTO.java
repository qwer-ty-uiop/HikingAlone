package com.ty.hikingalone.interfaces.user.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserChangePasswordDTO {
    @Email
    private String email;
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
}
