package com.ty.hikingalone.interfaces.user.dto.query;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileStatisticsDTO {

    @NotBlank
    Integer userId;

}
