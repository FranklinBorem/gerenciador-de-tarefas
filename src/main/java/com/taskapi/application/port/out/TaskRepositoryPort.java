package com.taskapi.application.port.out;

import com.taskapi.domain.model.PageResult;
import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.TaskStatus;

import java.util.Optional;

public interface TaskRepositoryPort {

    Task save(Task task);

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    /** Se status for nulo, traz tarefas de qualquer status. */
    PageResult<Task> findByUser(Long userId, TaskStatus status, int page, int size);

    void deleteById(Long id);
}
