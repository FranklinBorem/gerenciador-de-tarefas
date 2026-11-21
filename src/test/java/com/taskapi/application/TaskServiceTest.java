package com.taskapi.application;

import com.taskapi.application.port.in.CreateTaskUseCase;
import com.taskapi.application.service.TaskService;
import com.taskapi.domain.exception.BusinessRuleException;
import com.taskapi.domain.exception.ResourceNotFoundException;
import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Testa os casos de uso usando o banco em memória, sem Spring e sem Mockito. */
class TaskServiceTest {

    private static final Long ANA = 1L;
    private static final Long BRUNO = 2L;

    private final TaskService service = new TaskService(new InMemoryTaskRepository());

    private Task createTask(Long userId, String title) {
        return service.create(userId, new CreateTaskUseCase.Command(title, null, null));
    }

    @Test
    void createdTaskIsSavedAsPending() {
        Task task = createTask(ANA, "Estudar");

        assertEquals(TaskStatus.PENDING, service.get(ANA, task.getId()).getStatus());
    }

    @Test
    void taskFromAnotherUserIsNotFound() {
        Task task = createTask(ANA, "Tarefa da Ana");

        assertThrows(ResourceNotFoundException.class, () -> service.get(BRUNO, task.getId()));
    }

    @Test
    void doneTaskCannotGoBackToPending() {
        Task task = createTask(ANA, "Estudar");
        service.changeStatus(ANA, task.getId(), TaskStatus.DONE);

        assertThrows(BusinessRuleException.class,
                () -> service.changeStatus(ANA, task.getId(), TaskStatus.PENDING));
    }

    @Test
    void listFiltersByStatusAndOwner() {
        Task done = createTask(ANA, "Concluída");
        createTask(ANA, "Pendente");
        createTask(BRUNO, "Do Bruno");
        service.changeStatus(ANA, done.getId(), TaskStatus.DONE);

        assertEquals(2, service.list(ANA, null, 0, 10).content().size());
        assertEquals(1, service.list(ANA, TaskStatus.DONE, 0, 10).content().size());
    }

    @Test
    void deletedTaskIsGone() {
        Task task = createTask(ANA, "Apagar");

        service.delete(ANA, task.getId());

        assertThrows(ResourceNotFoundException.class, () -> service.get(ANA, task.getId()));
    }
}
