package com.ty.hikingalone.domain.user.repository;

import com.ty.hikingalone.domain.user.entity.UserAccount;

import java.util.List;

public interface UserAccountRepository {

    Long save(UserAccount userAccount);

    Long update(UserAccount userAccount);

    Long delete(Long id);

    UserAccount findById(Long id);

    List<UserAccount> findAll();

}
