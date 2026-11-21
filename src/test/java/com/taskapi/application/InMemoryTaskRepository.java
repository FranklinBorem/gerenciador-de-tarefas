package com.taskapi.application;

import com.taskapi.application.port.out.TaskRepositoryPort;
import com.taskapi.domain.model.PageResult;
import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.TaskStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Banco "de mentira" que guarda tudo na memória. Só é possível porque o núcleo depende de uma porta. */
class InMemoryTaskRepository implements TaskRepositoryPort {

    private final Map<Long, Task> store = new HashMap<>();
    private long nextId = 1;

    @Override
    public Task save(Task task) {
        Task toSave = task.getId() != null ? task : Task.restore(nextId++, task.getTitle(),
                task.getDescription(), task.getStatus(), task.getDueDate(), task.getCreatedAt(), task.getUserId());
        store.put(toSave.getId(), toSave);
        return toSave;
    }

    @Override
    public Optional<Task> findByIdAndUserId(Long id, Long userId) {
        return Optional.ofNullable(store.get(id)).filter(t -> t.getUserId().equals(userId));
    }

    @Override
    public PageResult<Task> findByUser(Long userId, TaskStatus status, int page, int size) {
        List<Task> all = store.values().stream()
                .filter(t -> t.getUserId().equals(userId))
                .filter(t -> status == null || t.getStatus() == status)
                .toList();
        return new PageResult<>(all, page, size, all.size(), 1);
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
