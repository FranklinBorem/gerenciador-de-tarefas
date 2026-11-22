package com.taskapi.adapter.out.persistence;

import com.taskapi.application.port.out.TaskRepositoryPort;
import com.taskapi.domain.model.PageResult;
import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Implementa a porta de saída usando JPA e PostgreSQL. */
@Component
public class TaskPersistenceAdapter implements TaskRepositoryPort {

    private final SpringDataTaskRepository repository;

    public TaskPersistenceAdapter(SpringDataTaskRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Task save(Task task) {
        return PersistenceMapper.taskToDomain(repository.save(PersistenceMapper.taskToEntity(task)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> findByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId).map(PersistenceMapper::taskToDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Task> findByUser(Long userId, TaskStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TaskEntity> result = (status == null)
                ? repository.findByUserId(userId, pageable)
                : repository.findByUserIdAndStatus(userId, status, pageable);

        return new PageResult<>(
                result.getContent().stream().map(PersistenceMapper::taskToDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
