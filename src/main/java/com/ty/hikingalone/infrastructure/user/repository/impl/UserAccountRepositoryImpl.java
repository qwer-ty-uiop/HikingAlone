package com.ty.hikingalone.infrastructure.user.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        userAccountMapper.insert(accountPO);
        userAccount.setId(accountPO.getId());
        return accountPO.getId();
    }

    @Override
    public Long update(UserAccount userAccount) {
        UserAccountPO accountPO = UserAccountPO.builder()
                .id(userAccount.getId())
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
        return accountPO == null ? null : toEntity(accountPO);
    }

    @Override
    public UserAccount findByUsername(String username) {
        UserAccountPO accountPO = userAccountMapper.selectOne(
                new LambdaQueryWrapper<UserAccountPO>()
                        .eq(UserAccountPO::getUsername, username)
        );
        return accountPO == null ? null : toEntity(accountPO);
    }

    @Override
    public UserAccount findByEmail(String email) {
        UserAccountPO accountPO = userAccountMapper.selectOne(
                new LambdaQueryWrapper<UserAccountPO>()
                        .eq(UserAccountPO::getEmail, email)
        );
        return accountPO == null ? null : toEntity(accountPO);
    }

    @Override
    public List<UserAccount> findAll() {
        return userAccountMapper.selectList(null)
                .stream()
                .map(this::toEntity)
                .toList();
    }

    private UserAccount toEntity(UserAccountPO accountPO) {
        return UserAccount.reconstruct(
                accountPO.getId(), accountPO.getUsername(), accountPO.getPassword(), accountPO.getEmail(),
                accountPO.getCreateTime(), accountPO.getUpdateTime());
    }
}
