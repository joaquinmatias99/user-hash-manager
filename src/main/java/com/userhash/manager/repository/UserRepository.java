package com.userhash.manager.repository;

import com.userhash.manager.model.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    UserEntity save(UserEntity userEntity);
    Optional<UserEntity> findByUsername(String username);
    void delete(UserEntity userEntity);
    List<UserEntity> findAll();
}
