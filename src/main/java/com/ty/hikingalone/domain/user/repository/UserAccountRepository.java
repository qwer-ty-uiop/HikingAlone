package com.ty.hikingalone.domain.user.repository;

import com.ty.hikingalone.domain.user.entity.UserAccount;

import java.util.List;

public interface UserAccountRepository {

    /**
     * 保存账号（回填主键），返回主键 id
     */
    Long save(UserAccount userAccount);

    /**
     * 更新账号字段，返回受影响行数
     */
    Long update(UserAccount userAccount);

    /**
     * 删除账号，返回受影响行数
     */
    Long delete(Long id);

    UserAccount findById(Long id);

    UserAccount findByUsername(String username);

    UserAccount findByEmail(String email);

    List<UserAccount> findAll();

}
