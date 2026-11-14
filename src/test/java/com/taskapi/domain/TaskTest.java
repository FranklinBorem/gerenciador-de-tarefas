package com.taskapi.domain;

import com.taskapi.domain.exception.BusinessRuleException;
import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Testa as regras do domínio sem Spring, sem banco e sem Mockito. */
class TaskTest {

    @Test
    void newTaskStartsAsPending() {
        Task task = Task.create("  Estudar Java  ", "Spring", LocalDate.now().plusDays(1), 1L);

        assertEquals("Estudar Java", task.getTitle());
        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    @Test
    void blankTitleIsRejected() {
        assertThrows(BusinessRuleException.class, () -> Task.create("   ", null, null, 1L));
    }

    @Test
    void doneTaskCannotGoBackToPending() {
        Task task = Task.create("Estudar", null, null, 1L);
        task.changeStatus(TaskStatus.DONE);

        assertThrows(BusinessRuleException.class, () -> task.changeStatus(TaskStatus.PENDING));
    }

    @Test
    void doneTaskCanGoBackToInProgress() {
        Task task = Task.create("Estudar", null, null, 1L);
        task.changeStatus(TaskStatus.DONE);

        task.changeStatus(TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    void updateChangesTitleDescriptionAndDueDate() {
        Task task = Task.create("Antigo", "x", null, 1L);
        LocalDate date = LocalDate.now().plusDays(5);

        task.update("Novo", "y", date);

        assertEquals("Novo", task.getTitle());
        assertEquals("y", task.getDescription());
        assertEquals(date, task.getDueDate());
    }
}
