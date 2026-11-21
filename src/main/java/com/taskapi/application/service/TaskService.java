package com.taskapi.application.service;

import com.taskapi.application.port.in.ChangeTaskStatusUseCase;
import com.taskapi.application.port.in.CreateTaskUseCase;
import com.taskapi.application.port.in.DeleteTaskUseCase;
import com.taskapi.application.port.in.GetTaskUseCase;
import com.taskapi.application.port.in.ListTasksUseCase;
import com.taskapi.application.port.in.UpdateTaskUseCase;
import com.taskapi.application.port.out.TaskRepositoryPort;
import com.taskapi.domain.exception.ResourceNotFoundException;
import com.taskapi.domain.model.PageResult;
import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.TaskStatus;

/** Casos de uso das tarefas. Não conhece Spring, banco nem HTTP: só portas e domínio. */
public class TaskService implements CreateTaskUseCase, ListTasksUseCase, GetTaskUseCase,
        UpdateTaskUseCase, ChangeTaskStatusUseCase, DeleteTaskUseCase {

    private static final int MAX_PAGE_SIZE = 50;

    private final TaskRepositoryPort tasks;

    public TaskService(TaskRepositoryPort tasks) {
        this.tasks = tasks;
    }

    @Override
    public Task create(Long userId, CreateTaskUseCase.Command command) {
        Task task = Task.create(command.title(), command.description(), command.dueDate(), userId);
        return tasks.save(task);
    }

    @Override
    public PageResult<Task> list(Long userId, TaskStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return tasks.findByUser(userId, status, safePage, safeSize);
    }

    @Override
    public Task get(Long userId, Long taskId) {
        return findOwned(userId, taskId);
    }

    @Override
    public Task update(Long userId, Long taskId, UpdateTaskUseCase.Command command) {
        Task task = findOwned(userId, taskId);
        task.update(command.title(), command.description(), command.dueDate());
        return tasks.save(task);
    }

    @Override
    public Task changeStatus(Long userId, Long taskId, TaskStatus newStatus) {
        Task task = findOwned(userId, taskId);
        task.changeStatus(newStatus);
        return tasks.save(task);
    }

    @Override
    public void delete(Long userId, Long taskId) {
        Task task = findOwned(userId, taskId);
        tasks.deleteById(task.getId());
    }

    // Busca só entre as tarefas do próprio usuário: tarefa de outra pessoa responde "não encontrada".
    private Task findOwned(Long userId, Long taskId) {
        return tasks.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
    }
}
