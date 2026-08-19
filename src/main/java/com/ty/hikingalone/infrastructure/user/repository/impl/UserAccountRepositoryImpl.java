package com.ty.hikingalone.infrastructure.user.repository.impl;

import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.domain.user.repository.UserAccountRepository;
import com.ty.hikingalone.infrastructure.user.mapper.UserAccountMapper;
import com.ty.hikingalone.infrastructure.user.po.UserAccountPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final UserAccountMapper userAccountMapper;

    @Override
    public Long save(UserAccount userAccount) {
        UserAccountPO accountPO = UserAccountPO.builder()
                .email(userAccount.getEmail())
                .password(userAccount.getPassword())
                .username(userAccount.getUsername())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        return (long) userAccountMapper.insert(accountPO);
    }

    @Override
    public Long update(UserAccount userAccount) {
        UserAccountPO accountPO = UserAccountPO.builder()
                .email(userAccount.getEmail())
                .password(userAccount.getPassword())
                .username(userAccount.getUsername())
                .updateTime(LocalDateTime.now())
                .build();
        return (long) userAccountMapper.updateById(accountPO);
    }

    @Override
    public Long delete(Long id) {
        return (long) userAccountMapper.deleteById(id);
    }

    @Override
    public UserAccount findById(Long id) {
        UserAccountPO accountPO = userAccountMapper.selectById(id);
        return toEntity(accountPO);
    }

    @Override
    public List<UserAccount> findAll() {
        return userAccountMapper.selectList(null)
                .stream()
                .map(this::toEntity)
                .toList();
    }

    private UserAccount toEntity(UserAccountPO accountPO) {
        return UserAccount.builder()
                .id(accountPO.getId())
                .username(accountPO.getUsername())
                .password(accountPO.getPassword())
                .email(accountPO.getEmail())
                .createTime(accountPO.getCreateTime())
                .updateTime(accountPO.getUpdateTime())
                .build();
    }
}
