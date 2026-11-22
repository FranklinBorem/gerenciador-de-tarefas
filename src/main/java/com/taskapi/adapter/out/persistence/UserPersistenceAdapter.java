package com.taskapi.adapter.out.persistence;

import com.taskapi.application.port.out.UserRepositoryPort;
import com.taskapi.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public UserPersistenceAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public User save(User user) {
        return PersistenceMapper.userToDomain(repository.save(PersistenceMapper.userToEntity(user)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(PersistenceMapper::userToDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
