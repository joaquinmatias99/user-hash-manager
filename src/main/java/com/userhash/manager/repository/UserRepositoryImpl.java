package com.userhash.manager.repository;

import com.userhash.manager.model.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        return userJpaRepository.save(userEntity);
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return userJpaRepository.findByUsername(username);
    }

    @Override
    public void delete(UserEntity userEntity) {
        userJpaRepository.delete(userEntity);
    }

    @Override
    public List<UserEntity> findAll() {
        return userJpaRepository.findAll();
    }
}
