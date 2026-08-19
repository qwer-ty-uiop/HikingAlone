package com.ty.hikingalone.interfaces.user.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @Email
    private String email;
}
