package com.taskapi.adapter.in.web;

import com.taskapi.adapter.in.web.dto.PageResponse;
import com.taskapi.adapter.in.web.dto.StatusRequest;
import com.taskapi.adapter.in.web.dto.TaskRequest;
import com.taskapi.adapter.in.web.dto.TaskResponse;
import com.taskapi.application.port.in.ChangeTaskStatusUseCase;
import com.taskapi.application.port.in.CreateTaskUseCase;
import com.taskapi.application.port.in.DeleteTaskUseCase;
import com.taskapi.application.port.in.GetTaskUseCase;
import com.taskapi.application.port.in.ListTasksUseCase;
import com.taskapi.application.port.in.UpdateTaskUseCase;
import com.taskapi.domain.model.PageResult;
import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.TaskStatus;
import com.taskapi.domain.model.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tarefas")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final CreateTaskUseCase createTask;
    private final ListTasksUseCase listTasks;
    private final GetTaskUseCase getTask;
    private final UpdateTaskUseCase updateTask;
    private final ChangeTaskStatusUseCase changeTaskStatus;
    private final DeleteTaskUseCase deleteTask;

    public TaskController(CreateTaskUseCase createTask,
                          ListTasksUseCase listTasks,
                          GetTaskUseCase getTask,
                          UpdateTaskUseCase updateTask,
                          ChangeTaskStatusUseCase changeTaskStatus,
                          DeleteTaskUseCase deleteTask) {
        this.createTask = createTask;
        this.listTasks = listTasks;
        this.getTask = getTask;
        this.updateTask = updateTask;
        this.changeTaskStatus = changeTaskStatus;
        this.deleteTask = deleteTask;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody TaskRequest request) {
        Task task = createTask.create(user.getId(),
                new CreateTaskUseCase.Command(request.title(), request.description(), request.dueDate()));
        return TaskResponse.from(task);
    }

    @GetMapping
    public PageResponse<TaskResponse> list(@AuthenticationPrincipal User user,
                                           @RequestParam(name = "status", required = false) TaskStatus status,
                                           @RequestParam(name = "page", defaultValue = "0") int page,
                                           @RequestParam(name = "size", defaultValue = "10") int size) {
        PageResult<Task> result = listTasks.list(user.getId(), status, page, size);
        return new PageResponse<>(
                result.content().stream().map(TaskResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @GetMapping("/{id}")
    public TaskResponse get(@AuthenticationPrincipal User user, @PathVariable("id") Long id) {
        return TaskResponse.from(getTask.get(user.getId(), id));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal User user,
                               @PathVariable("id") Long id,
                               @Valid @RequestBody TaskRequest request) {
        Task task = updateTask.update(user.getId(), id,
                new UpdateTaskUseCase.Command(request.title(), request.description(), request.dueDate()));
        return TaskResponse.from(task);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse changeStatus(@AuthenticationPrincipal User user,
                                     @PathVariable("id") Long id,
                                     @Valid @RequestBody StatusRequest request) {
        return TaskResponse.from(changeTaskStatus.changeStatus(user.getId(), id, request.status()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable("id") Long id) {
        deleteTask.delete(user.getId(), id);
    }
}
