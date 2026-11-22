package com.taskapi.adapter.out.persistence;

import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.User;

/** Converte entre as classes do domínio e as classes do banco. */
final class PersistenceMapper {

    private PersistenceMapper() {
    }

    static Task taskToDomain(TaskEntity e) {
        return Task.restore(e.getId(), e.getTitle(), e.getDescription(), e.getStatus(),
                e.getDueDate(), e.getCreatedAt(), e.getUserId());
    }

    static TaskEntity taskToEntity(Task t) {
        return new TaskEntity(t.getId(), t.getTitle(), t.getDescription(), t.getStatus(),
                t.getDueDate(), t.getCreatedAt(), t.getUserId());
    }

    static User userToDomain(UserEntity e) {
        return new User(e.getId(), e.getName(), e.getEmail(), e.getPasswordHash());
    }

    static UserEntity userToEntity(User u) {
        return new UserEntity(u.getId(), u.getName(), u.getEmail(), u.getPasswordHash());
    }
}
