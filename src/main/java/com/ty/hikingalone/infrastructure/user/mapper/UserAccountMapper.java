package com.ty.hikingalone.infrastructure.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.user.po.UserAccountPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccountPO> {
}
